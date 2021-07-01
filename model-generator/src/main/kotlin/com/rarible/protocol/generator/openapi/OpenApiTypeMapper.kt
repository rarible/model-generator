package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeMapper
import com.rarible.protocol.generator.component.AbstractComponent
import com.rarible.protocol.generator.component.ComponentField
import com.rarible.protocol.generator.component.Discriminator
import com.rarible.protocol.generator.component.GeneratedComponent
import com.rarible.protocol.generator.exception.SchemaValidationException
import com.reprezen.kaizen.oasparser.OpenApi3Parser
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.*

class OpenApiTypeMapper(
    private val qualifierGenerator: QualifierGenerator,
    private val primitiveTypeMapper: OpenApiPrimitiveTypeMapper,
    private val providedTypeMapper: OpenApiProvidedTypeMapper
) : TypeMapper {

    private val log = LoggerFactory.getLogger(OpenApiTypeMapper::class.java)
    private val generatedComponents: MutableMap<String, GeneratedComponent> = LinkedHashMap()

    override fun readGeneratedComponents(path: Path): List<GeneratedComponent> {
        val openApi = OpenApi3Parser().parse(path.toFile())
        return readGeneratedComponents(openApi)
    }

    private fun readGeneratedComponents(openApi: OpenApi3): List<GeneratedComponent> {
        log.debug("Starting to read OpenAPI schema...")
        generatedComponents.clear()
        for (schema in openApi.schemas.values) {
            getOrCreateDefinition(OpenApiComponent(schema))
        }
        return ArrayList(generatedComponents.values)
    }

    private fun getOrCreateDefinition(component: OpenApiComponent): AbstractComponent {
        log.debug("Reading component: ${component.name}")
        val isProvided = providedTypeMapper.has(component.name)

        // Checking provided first, then - generated
        if (isProvided) {
            log.debug("Component ${component.name} is provided type, skipping generation")
            return providedTypeMapper.getDefinition(component.name)
        } else {
            return getOrCreateGeneratedDefinition(component)
        }
    }

    private fun getOrCreateGeneratedDefinition(component: OpenApiComponent): GeneratedComponent {
        val result = generatedComponents[component.name]
        if (result != null) {
            log.debug("Component ${component.name} already generated, using existing reference")
            return result
        }
        return createDefinition(component)
    }

    private fun createDefinition(component: OpenApiComponent): GeneratedComponent {
        val fields = component.getFields().associateByTo(LinkedHashMap(),
            { it.name }, { createFieldDefinition(it) }
        )

        val enums = component.getEnums()

        if (enums.isNotEmpty() && fields.isNotEmpty()) {
            throw SchemaValidationException("Component ${component.name} contains both enums and fields")
        }

        val generatedComponentDefinition = GeneratedComponent(
            component.name,
            qualifierGenerator.getQualifier(component.name),
            enums,
            fields,
            getDiscriminator(component)
        )

        log.debug("-------------- Component ${component.name} read --------------\n$generatedComponentDefinition")
        log.debug("--------------------------------------------------------------")

        generatedComponents[component.name] = generatedComponentDefinition
        return generatedComponentDefinition
    }

    private fun getDiscriminator(component: OpenApiComponent): Discriminator? {
        // Gathering GeneratedComponentDefinitions for OneOf list
        if (!component.isOneOf()) {
            return null
        }

        log.debug("Reading discriminator: ${component.name}")

        val discriminatorField = component.getDiscriminatorField()
        log.debug("Discriminator field: $discriminatorField")

        val componentMapping = LinkedHashMap<String, GeneratedComponent>()
        for (oneOfComponent in component.getOneOf()) {
            val generatedComponent = getOrCreateGeneratedDefinition(oneOfComponent)
            if (oneOfComponent.isOneOf()) {
                componentMapping[generatedComponent.name] = generatedComponent
            } else {
                val field = oneOfComponent.getField(discriminatorField)
                val mappingValue = field.getOneOfEnumValue()
                componentMapping[mappingValue] = generatedComponent
            }
        }

        val result = Discriminator(discriminatorField, componentMapping)
        log.debug("Discriminator: $result")
        return result
    }

    private fun createFieldDefinition(
        field: OpenApiField
    ): ComponentField {
        log.debug("Reading field: ${field.fullName}")
        var fieldTypeDefinition: AbstractComponent
        var fieldGenericTypes: List<AbstractComponent> = Collections.emptyList()
        var fieldEnumValues = listOf<String>()

        if (field.isArray()) {
            fieldEnumValues = field.enumValues
            fieldTypeDefinition = primitiveTypeMapper.getDefinition(field.type)
            if (field.isArrayOfPrimitives()) {
                val (type, format) = field.getArrayPrimitiveType()
                fieldEnumValues = field.getArrayEnums()
                log.debug("--- ${field.fullName} -> array of primitives (type = $type, format = $format, enums = $fieldEnumValues")
                val primitiveType = primitiveTypeMapper.getDefinition(type, format)
                log.debug("--- ${field.fullName} -> array primitive definition found: $primitiveType")
                fieldGenericTypes = listOf(primitiveType)
            } else {
                val component = field.getArrayComponent()
                log.debug("--- ${field.fullName} -> array of references ($component)")
                fieldGenericTypes = listOf(getOrCreateDefinition(field.getArrayComponent()))
            }
        } else if (field.isCreatingReference() || generatedComponents.containsKey(field.getReferencedSchemaName())) {
            val component = field.getComponent()
            log.debug("--- ${field.fullName} -> reference ($component)")
            fieldTypeDefinition = getOrCreateDefinition(component)
        } else if (field.isMap()) {
            fieldTypeDefinition = primitiveTypeMapper.getDefinition("map")
            val stringType = primitiveTypeMapper.getDefinition("string")
            if (field.isMapOfPrimitives()) {
                val (type, format) = field.getMapPrimitiveType()
                log.debug("--- ${field.fullName} -> map of primitives (type = $type, format = $format)")
                val primitiveType = primitiveTypeMapper.getDefinition(type, format)
                log.debug("--- ${field.fullName} -> map primitive definition found: $primitiveType")
                fieldGenericTypes = listOf(stringType, primitiveType)
            } else {
                val component = field.getMapComponent()
                log.debug("--- ${field.fullName} -> array of references ($component)")
                fieldGenericTypes = listOf(stringType, getOrCreateDefinition(component))
            }
        } else {
            fieldEnumValues = field.enumValues
            log.debug("--- ${field.fullName} -> primitive (type = ${field.type}, format = ${field.format})")
            // Otherwise, this is one of primitive types like String or Integer
            fieldTypeDefinition = primitiveTypeMapper.getDefinition(field.type, field.format)
            log.debug("--- ${field.fullName} -> primitive definition found: $fieldTypeDefinition")
        }

        val result = ComponentField(
            field.name,
            fieldTypeDefinition,
            fieldGenericTypes,
            fieldEnumValues,
            field.required
        )

        log.debug("Field read: $result")

        return result
    }
}
