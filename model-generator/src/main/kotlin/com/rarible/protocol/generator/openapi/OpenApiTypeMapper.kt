package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeDefinitionMapper
import com.rarible.protocol.generator.component.AbstractComponent
import com.rarible.protocol.generator.component.ComponentField
import com.rarible.protocol.generator.component.Discriminator
import com.rarible.protocol.generator.component.GeneratedComponent
import com.rarible.protocol.generator.exception.SchemaValidationException
import com.reprezen.kaizen.oasparser.OpenApi3Parser
import java.nio.file.Path
import java.util.*

class OpenApiTypeMapper(
    private val qualifierGenerator: QualifierGenerator,
    private val primitiveTypeMapper: OpenApiPrimitiveTypeMapper,
    private val providedTypeMapper: OpenApiProvidedTypeMapper
) : TypeDefinitionMapper {

    private val generatedComponents: MutableMap<String, GeneratedComponent> = LinkedHashMap()

    override fun readGeneratedComponents(path: Path): List<GeneratedComponent> {
        val openApi = OpenApi3Parser().parse(path.toFile())
        for (schema in openApi.schemas.values) {
            getOrCreateDefinition(OpenApiComponent(schema))
        }
        return ArrayList(generatedComponents.values)
    }

    private fun getOrCreateDefinition(component: OpenApiComponent): AbstractComponent {
        val isProvided = providedTypeMapper.has(component.name)
        if (!component.isObject() && !component.isOneOf() && !isProvided) {
            throw SchemaValidationException("There is no provided type for custom component '${component.name}'")
        }

        // Checking provided first, then - generated
        return if (isProvided)
            providedTypeMapper.getDefinition(component.name)
        else
            getOrCreateGeneratedDefinition(component)
    }

    private fun getOrCreateGeneratedDefinition(component: OpenApiComponent): GeneratedComponent {
        return generatedComponents[component.name] ?: createDefinition(component)
    }

    private fun createDefinition(component: OpenApiComponent): GeneratedComponent {
        val fields = component.getFields().associateByTo(LinkedHashMap(),
            { it.name }, { createFieldDefinition(it) }
        )

        var discriminator: Discriminator? = null
        // Gathering GeneratedComponentDefinitions for OneOf list
        if (component.isOneOf()) {
            val discriminatorField = component.getDiscriminatorField()
            val discriminatorMapping = LinkedHashMap<String, GeneratedComponent>()
            for (oneOfComponent in component.getOneOf()) {
                val field = oneOfComponent.getField(discriminatorField)
                val mappingValue = field.getOneOfEnumValue()
                val generatedComponent = getOrCreateGeneratedDefinition(oneOfComponent)
                discriminatorMapping[mappingValue] = generatedComponent
            }
            discriminator = Discriminator(discriminatorField, discriminatorMapping)
        }


        val generatedComponentDefinition = GeneratedComponent(
            component.name,
            qualifierGenerator.getQualifier(component.name),
            fields,
            discriminator
        )

        generatedComponents[component.name] = generatedComponentDefinition
        return generatedComponentDefinition
    }

    private fun createFieldDefinition(
        field: OpenApiField
    ): ComponentField {

        var fieldTypeDefinition: AbstractComponent
        var fieldGenericTypes: List<AbstractComponent> = Collections.emptyList()
        var fieldEnumValues = field.enumValues

        if (field.isArray()) {
            fieldTypeDefinition = primitiveTypeMapper.getDefinition(field.type)
            if (field.isArrayOfPrimitives()) {
                val (type, format) = field.getArrayPrimitiveType()
                val primitiveType = primitiveTypeMapper.getDefinition(type, format)
                fieldEnumValues = field.getArrayEnums()
                fieldGenericTypes = listOf(primitiveType)
            } else {
                fieldGenericTypes = listOf(getOrCreateDefinition(field.getArrayComponent()))
            }
        } else if (field.isMap()) {
            fieldTypeDefinition = primitiveTypeMapper.getDefinition("map")
            val stringType = primitiveTypeMapper.getDefinition("string")
            if (field.isMapOfPrimitives()) {
                val (type, format) = field.getMapPrimitiveType()
                val primitiveType = primitiveTypeMapper.getDefinition(type, format)
                fieldGenericTypes = listOf(stringType, primitiveType)
            } else {
                fieldGenericTypes = listOf(stringType, getOrCreateDefinition(field.getMapComponent()))
            }
        } else if (field.isReference()) {
            fieldTypeDefinition = getOrCreateDefinition(field.getComponent())
        } else {
            // Otherwise, this is one of primitive types like String or Integer
            fieldTypeDefinition = primitiveTypeMapper.getDefinition(field.type, field.format)
        }

        return ComponentField(
            field.name,
            fieldTypeDefinition,
            fieldGenericTypes,
            fieldEnumValues,
            field.required
        )
    }
}
