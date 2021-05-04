package com.rarible.protocol.generator

import com.rarible.protocol.generator.component.ComponentDefinition
import com.rarible.protocol.generator.component.FieldDefinition
import com.rarible.protocol.generator.component.GeneratedComponentDefinition
import com.rarible.protocol.generator.component.SimpleComponentDefinition
import com.rarible.protocol.generator.type.ExternalTypeDefinition
import com.rarible.protocol.generator.type.InternalTypeDefinition
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Schema
import com.reprezen.kaizen.oasparser.ovl3.SchemaImpl
import java.util.*

class OpenApiTypeDefinitionMapper(
    private val packageName: String,
    internalTypeDefinitions: List<InternalTypeDefinition>,
    externalTypeDefinitions: List<ExternalTypeDefinition>
) {

    private val primitiveDefinitions: Map<String, ComponentDefinition>
    private val componentDefinitions: MutableMap<String, ComponentDefinition>

    init {
        primitiveDefinitions = internalTypeDefinitions.associateByTo(HashMap(),
            { it.openApiType }, { toSimpleComponent(it) }
        )
        componentDefinitions = externalTypeDefinitions.associateByTo(HashMap(),
            { it.componentName }, { toSimpleComponent(it) }
        )
    }

    fun getGeneratedComponents(openApi: OpenApi3): List<ComponentDefinition> {
        val schemas = openApi.schemas.values
        val result = ArrayList<ComponentDefinition>()
        for (schema in schemas) {
            val definition = getComponentDefinition(schema);
            // TODO maybe there is better way to check it?
            if (definition is GeneratedComponentDefinition) {
                result.add(definition)
            }
        }
        return result;
    }

    private fun getComponentDefinition(schema: Schema): ComponentDefinition {
        val type = componentDefinitions[schema.name]
        // We already described this component or this is one of External components
        if (type != null) {
            return type
        }

        val generatedComponentDefinition = GeneratedComponentDefinition(schema.name, packageName)

        val fields: MutableMap<String, FieldDefinition> = LinkedHashMap()
        val requiredFieldNames = HashSet(schema.requiredFields)

        for ((fieldName, fieldSchema) in schema.properties) {
            val isRequired = requiredFieldNames.contains(fieldName)
            val fieldDefinition = createFieldDefinition(fieldName, isRequired, fieldSchema)
            fields[fieldName] = fieldDefinition
        }

        generatedComponentDefinition.fields = fields
        componentDefinitions[schema.name] = generatedComponentDefinition

        return generatedComponentDefinition
    }

    private fun createFieldDefinition(
        fieldName: String,
        isRequired: Boolean,
        fieldSchema: Schema
    ): FieldDefinition {

        var fieldTypeDefinition: ComponentDefinition
        var fieldGenericTypes: List<ComponentDefinition> = Collections.emptyList()

        if ("array" == fieldSchema.type) {
            fieldTypeDefinition = primitiveDefinitions["array"]!!
            val genericDefinition = getComponentDefinition(fieldSchema.itemsSchema)
            fieldGenericTypes = listOf(genericDefinition)

        } else if ((fieldSchema as SchemaImpl)._getCreatingRef() != null) {
            // The only way to define our type is referenced, not primitive
            fieldTypeDefinition = getComponentDefinition(fieldSchema)
        } else {
            // Otherwise, this is one of primitive types like String or Integer
            fieldTypeDefinition = primitiveDefinitions[fieldSchema.type]!!
        }

        return FieldDefinition(
            fieldName,
            fieldTypeDefinition,
            fieldGenericTypes,
            isRequired
        )
    }

    private fun toSimpleComponent(definition: ExternalTypeDefinition): SimpleComponentDefinition {
        return SimpleComponentDefinition(
            definition.javaClass.simpleName,
            definition.javaClass.`package`.name,
            definition.componentName
        )
    }

    private fun toSimpleComponent(definition: InternalTypeDefinition): SimpleComponentDefinition {
        return SimpleComponentDefinition(
            definition.javaClass.simpleName,
            definition.javaClass.`package`.name,
            definition.openApiType
        )
    }
}
