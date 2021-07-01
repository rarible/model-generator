package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.exception.IllegalOperationException
import com.reprezen.kaizen.oasparser.model3.Schema

class OpenApiComponent(
    private val schema: Schema
) {

    val name: String = schema.name

    private val requiredFields = HashSet(schema.requiredFields)

    fun isOneOf(): Boolean {
        return schema.oneOfSchemas.isNotEmpty()
    }

    fun getField(fieldName: String): OpenApiField {
        val fieldSchema = schema.properties[fieldName]
            ?: throw IllegalOperationException("Field '$name: $fieldName' not found")

        return OpenApiField(fieldName, this, fieldSchema, requiredFields.contains(fieldSchema.name))
    }

    fun getFields(): List<OpenApiField> {
        return schema.properties.map {
            OpenApiField(it.key, this, it.value, requiredFields.contains(it.key))
        }
    }

    fun getEnums(): List<String> {
        return schema.enums.map { it.toString() }
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
            throw IllegalOperationException("Component '$name' is not an oneOf component")
        }
    }

    override fun toString(): String {
        return name
    }
}
