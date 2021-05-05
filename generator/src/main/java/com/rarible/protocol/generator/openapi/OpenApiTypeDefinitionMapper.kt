package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.SchemaValidationException
import com.rarible.protocol.generator.TypeDefinitionMapper
import com.rarible.protocol.generator.component.ComponentDefinition
import com.rarible.protocol.generator.component.FieldDefinition
import com.rarible.protocol.generator.component.GeneratedComponentDefinition
import com.rarible.protocol.generator.component.SimpleComponentDefinition
import com.reprezen.kaizen.oasparser.OpenApi3Parser
import com.reprezen.kaizen.oasparser.model3.Schema
import com.reprezen.kaizen.oasparser.ovl3.SchemaImpl
import java.nio.file.Path
import java.util.*

class OpenApiTypeDefinitionMapper(
    private val qualifierGenerator: QualifierGenerator,
    primitiveTypeDefinitions: Map<String, String>,
    providedTypeDefinitions: Map<String, String>
) : TypeDefinitionMapper {

    private val primitiveDefinitions: Map<String, ComponentDefinition>
    private val providedComponentDefinitions: MutableMap<String, ComponentDefinition>

    private val generatedComponentDefinitions: MutableMap<String, GeneratedComponentDefinition> = LinkedHashMap()

    init {
        primitiveDefinitions = primitiveTypeDefinitions.mapValuesTo(HashMap()) {
            SimpleComponentDefinition(it.key, it.value)
        }

        providedComponentDefinitions = providedTypeDefinitions.mapValuesTo(HashMap()) {
            SimpleComponentDefinition(it.key, it.value)
        }
    }

    override fun readGeneratedComponents(path: Path): List<GeneratedComponentDefinition> {
        val openApi = OpenApi3Parser().parse(path.toFile())
        for (schema in openApi.schemas.values) {
            generateComponentDefinition(schema)
        }
        return ArrayList(generatedComponentDefinitions.values)
    }

    private fun generateComponentDefinition(schema: Schema): ComponentDefinition {
        checkProvidedComponent(schema)

        val type = getExistingComponentDefinition(schema.name)
        if (type != null) {
            return type
        }

        val fields: MutableMap<String, FieldDefinition> = LinkedHashMap()
        val requiredFieldNames = HashSet(schema.requiredFields)

        for ((fieldName, fieldSchema) in schema.properties) {
            val isRequired = requiredFieldNames.contains(fieldName)
            fields[fieldName] = createFieldDefinition(fieldName, isRequired, fieldSchema)
        }

        val oneOf = ArrayList<GeneratedComponentDefinition>()
        for (oneOfSchema in schema.oneOfSchemas) {
            oneOf.add(generateComponentDefinition(oneOfSchema) as GeneratedComponentDefinition)
        }

        val generatedComponentDefinition = GeneratedComponentDefinition(
            schema.name,
            qualifierGenerator.getQualifier(schema.name),
            fields,
            oneOf
        )

        generatedComponentDefinitions[schema.name] = generatedComponentDefinition
        return generatedComponentDefinition
    }

    private fun getExistingComponentDefinition(name: String): ComponentDefinition? {
        val type = providedComponentDefinitions[name]
        // This component is one of provided types
        if (type != null) {
            return type
        }

        return generatedComponentDefinitions[name]
    }

    private fun checkProvidedComponent(schema: Schema) {
        if (!"object".equals(schema.type) && schema.type != null) {
            if (!providedComponentDefinitions.containsKey(schema.name)) {
                throw SchemaValidationException("There is no provided type for custom component '${schema.name}'")
            }
        }
    }

    private fun createFieldDefinition(
        fieldName: String,
        isRequired: Boolean,
        fieldSchema: Schema
    ): FieldDefinition {

        var fieldTypeDefinition: ComponentDefinition
        var fieldGenericTypes: List<ComponentDefinition> = Collections.emptyList()
        var enumValues: List<String> = Collections.emptyList()

        if ("array" == fieldSchema.type) {
            fieldTypeDefinition = primitiveDefinitions["array"]!!
            val genericDefinition = generateComponentDefinition(fieldSchema.itemsSchema)
            fieldGenericTypes = listOf(genericDefinition)
        } else if ((fieldSchema as SchemaImpl)._getCreatingRef() != null) {
            // The only way to define our type is referenced, not primitive
            fieldTypeDefinition = generateComponentDefinition(fieldSchema)
        } else {
            // Otherwise, this is one of primitive types like String or Integer
            fieldTypeDefinition = primitiveDefinitions[fieldSchema.type]!!
            if (fieldTypeDefinition == null) {
                throw SchemaValidationException(
                    "There is no primitive type definition" +
                            " for OpenApi type'${fieldSchema.type}'"
                )
            }
            enumValues = fieldSchema.enums.map { it.toString() }
        }

        return FieldDefinition(
            fieldName,
            fieldTypeDefinition,
            fieldGenericTypes,
            enumValues,
            isRequired
        )
    }
}
