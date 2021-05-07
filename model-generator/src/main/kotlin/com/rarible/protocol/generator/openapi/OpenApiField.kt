package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.exception.IllegalOperationException
import com.reprezen.kaizen.oasparser.model3.Schema
import com.reprezen.kaizen.oasparser.ovl3.SchemaImpl

class OpenApiField(
    private val fieldSchema: Schema,
    val required: Boolean
) {

    val name: String = fieldSchema.name
    val enumValues: List<String> = fieldSchema.enums.map { it.toString() }
    val type: String = fieldSchema.type
    val format: String? = fieldSchema.format

    fun isArray(): Boolean {
        return fieldSchema.type == "array"
    }

    fun isMap(): Boolean {
        return fieldSchema.type == "object" && fieldSchema.additionalPropertiesSchema != null
    }

    fun getOneOfEnumValue(): String {
        if (enumValues.isEmpty()) {
            throw IllegalOperationException("Field '$name' has no oneOf enum maping")
        }
        return enumValues[0]
    }

    fun isReference(): Boolean {
        // The only way to define our type is referenced, not primitive
        return ((fieldSchema as SchemaImpl)._getCreatingRef() != null)
    }

    fun getComponent(): OpenApiComponent {
        if (!isReference()) {
            throw IllegalOperationException("Field '$name' if not a referenced type")
        }
        return OpenApiComponent(fieldSchema)
    }

    fun isMapOfPrimitives(): Boolean {
        assertIsMap()
        return fieldSchema.additionalPropertiesSchema.name == null
    }

    fun getMapPrimitiveType(): Pair<String, String> {
        assertIsMap()
        val type = fieldSchema.additionalPropertiesSchema.type
        val format = fieldSchema.additionalPropertiesSchema.format
        return Pair(type, format)
    }

    fun getMapComponent(): OpenApiComponent {
        assertIsMap()
        return OpenApiComponent(fieldSchema.additionalPropertiesSchema)
    }

    fun isArrayOfPrimitives(): Boolean {
        assertIsArray()
        return fieldSchema.itemsSchema.name == null
    }

    fun getArrayPrimitiveType(): Pair<String, String> {
        assertIsArray()
        val type = fieldSchema.itemsSchema.type
        val format = fieldSchema.itemsSchema.format
        return Pair(type, format)
    }

    fun getArrayEnums(): List<String> {
        return fieldSchema.itemsSchema.enums.map { it.toString() }
    }

    fun getArrayComponent(): OpenApiComponent {
        assertIsArray()
        return OpenApiComponent(fieldSchema.itemsSchema)
    }

    private fun assertIsArray() {
        if (!isArray()) {
            throw IllegalOperationException("Field '$name' if not an array type")
        }
    }

    private fun assertIsMap() {
        if (!isMap()) {
            throw IllegalOperationException("Field '$name' if not a map type")
        }
    }

}
