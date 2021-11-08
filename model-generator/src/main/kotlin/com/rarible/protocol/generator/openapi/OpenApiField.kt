package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.exception.IllegalOperationException
import com.rarible.protocol.generator.exception.SchemaValidationException
import com.reprezen.kaizen.oasparser.model3.Schema
import com.reprezen.kaizen.oasparser.ovl3.SchemaImpl

class OpenApiField(
    val name: String,
    owner: OpenApiComponent,
    private val fieldSchema: Schema,
    val required: Boolean
) {

    val type: String
    val format: String? = fieldSchema.format
    val fullName = "${owner.name}:$name"
    val enumValues: List<String> = fieldSchema.enums.filterNotNull().map { it.toString() }
    val defaultValue = fieldSchema.default
    val minimum: Number? = fieldSchema.minimum
    val maximum: Number? = fieldSchema.maximum
    val pattern: String? = fieldSchema.pattern

    init {
        // Case when reference is OneOf
        if (fieldSchema.type == null) {
            if (fieldSchema.oneOfSchemas.isNotEmpty()) {
                type = "object"
            } else {
                throw SchemaValidationException("Type not specified for field '$fullName'")
            }
        } else {
            type = fieldSchema.type
        }
        if (fieldSchema.name == null) {
            throw SchemaValidationException("There is no schema name for field '$fullName'")
        }
    }

    fun isArray(): Boolean {
        return fieldSchema.type == "array"
    }

    fun isMap(): Boolean {
        return fieldSchema.type == "object" && fieldSchema.additionalPropertiesSchema != null
    }

    fun getOneOfEnumValue(): String {
        if (enumValues.isEmpty()) {
            throw IllegalOperationException("Field '$fullName' has no oneOf enum mapping")
        }
        return enumValues[0]
    }

    fun getReferencedSchemaName(): String {
        return fieldSchema.name
    }

    fun isCreatingReference(): Boolean {
        // Returns true if type is reference, but reference is not created yet
        return ((fieldSchema as SchemaImpl)._getCreatingRef() != null)
    }

    fun getComponent(): OpenApiComponent {
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
        if (type == null) {
            throw SchemaValidationException("Item type for array field '${fullName}' is not defined")
        }
        return Pair(type, format)
    }

    fun getArrayEnums(): List<String> {
        assertIsArray()
        return fieldSchema.itemsSchema.enums.map { it.toString() }
    }

    fun getArrayComponent(): OpenApiComponent {
        assertIsArray()
        return OpenApiComponent(fieldSchema.itemsSchema)
    }

    private fun assertIsArray() {
        if (!isArray()) {
            throw IllegalOperationException("Field '$fullName' if not an array type")
        }
    }

    private fun assertIsMap() {
        if (!isMap()) {
            throw IllegalOperationException("Field '$fullName' if not a map type")
        }
    }

}
