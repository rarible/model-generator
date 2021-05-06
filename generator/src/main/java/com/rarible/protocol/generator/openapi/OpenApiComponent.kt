package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.exception.IllegalOperationException
import com.reprezen.kaizen.oasparser.model3.Schema

class OpenApiComponent(
    private val schema: Schema
) {

    val name: String = schema.name

    private val requiredFields = HashSet(schema.requiredFields)

    fun isObject(): Boolean {
        return "object" == schema.type
    }

    fun isOneOf(): Boolean {
        return schema.oneOfSchemas.isNotEmpty()
    }

    fun getField(fieldName: String): OpenApiField {
        val fieldSchema = schema.properties[fieldName]
            ?: throw IllegalOperationException("Field '$fieldName' not found in component '${name}'")

        return OpenApiField(fieldSchema, requiredFields.contains(fieldSchema.name))
    }

    fun getFields(): List<OpenApiField> {
        return schema.properties.map {
            OpenApiField(it.value, requiredFields.contains(it.key))
        }
    }

    fun getOneOf(): List<OpenApiComponent> {
        assertOneOf()
        return schema.oneOfSchemas.map { OpenApiComponent(it) }
    }

    fun getDiscriminatorField(): String {
        assertOneOf()
        return if (schema.discriminator == null || schema.discriminator.propertyName == null) "@type"
        else schema.discriminator.propertyName
    }

    private fun assertOneOf() {
        if (!isOneOf()) {
            throw IllegalOperationException("Component '$name' is not a OneOf component")
        }
    }
}
