package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.exception.IllegalOperationException
import com.rarible.protocol.generator.exception.SchemaValidationException
import com.reprezen.kaizen.oasparser.model3.Schema
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

internal class OpenApiFieldTest {

    @Test
    fun `test type not specified`() {
        val fieldSchema = mock(Schema::class.java)
        doReturn(ArrayList<String>()).`when`(fieldSchema).enums

        doReturn(ArrayList<String>()).`when`(fieldSchema).oneOfSchemas
        doReturn(null).`when`(fieldSchema).type

        assertThrows(SchemaValidationException::class.java) {
            OpenApiField("field", getDefaultComponent(), fieldSchema, false)
        }
    }

    @Test
    fun `test field schema name is not specified`() {
        val fieldSchema = mock(Schema::class.java)
        doReturn(null).`when`(fieldSchema).name
        doReturn(ArrayList<String>()).`when`(fieldSchema).enums
        doReturn(ArrayList<String>()).`when`(fieldSchema).oneOfSchemas
        doReturn("object").`when`(fieldSchema).type

        assertThrows(SchemaValidationException::class.java) {
            OpenApiField("field", getDefaultComponent(), fieldSchema, false)
        }
    }

    @Test
    fun `test get one of enum is not specified`() {
        val field = getDefaultObjectField()

        assertThrows(IllegalOperationException::class.java) {
            field.getOneOfEnumValue()
        }
    }

    @Test
    fun `test is not a map`() {
        val field = getDefaultObjectField()

        assertThrows(IllegalOperationException::class.java) { field.getMapPrimitiveType() }
        assertThrows(IllegalOperationException::class.java) { field.getMapComponent() }
    }

    @Test
    fun `test is not an array`() {
        val field = getDefaultObjectField()

        assertThrows(IllegalOperationException::class.java) { field.getArrayComponent() }
        assertThrows(IllegalOperationException::class.java) { field.getArrayPrimitiveType() }
        assertThrows(IllegalOperationException::class.java) { field.getArrayEnums() }
    }

    @Test
    fun `test array primitive type is not specified`() {
        val fieldSchema = mock(Schema::class.java)
        val itemsSchema = mock(Schema::class.java)
        doReturn("name").`when`(fieldSchema).name
        doReturn(ArrayList<String>()).`when`(fieldSchema).enums
        doReturn(ArrayList<String>()).`when`(fieldSchema).oneOfSchemas
        doReturn("array").`when`(fieldSchema).type
        doReturn(null).`when`(itemsSchema).type
        doReturn(ArrayList<String>()).`when`(itemsSchema).enums
        doReturn(itemsSchema).`when`(fieldSchema).itemsSchema

        val field = OpenApiField("field", getDefaultComponent(), fieldSchema, false)
        assertDoesNotThrow { field.getArrayEnums() }
        assertThrows(SchemaValidationException::class.java) { field.getArrayPrimitiveType() }
    }

    private fun getDefaultObjectField(): OpenApiField {
        val fieldSchema = mock(Schema::class.java)
        doReturn("name").`when`(fieldSchema).name
        doReturn(ArrayList<String>()).`when`(fieldSchema).enums
        doReturn(ArrayList<String>()).`when`(fieldSchema).oneOfSchemas
        doReturn("object").`when`(fieldSchema).type

        return OpenApiField("field", getDefaultComponent(), fieldSchema, false)
    }

    private fun getDefaultComponent(): OpenApiComponent {
        val schema = mock(Schema::class.java)
        doReturn("Item").`when`(schema).name
        doReturn(ArrayList<String>()).`when`(schema).requiredFields
        return OpenApiComponent(schema)
    }
}
