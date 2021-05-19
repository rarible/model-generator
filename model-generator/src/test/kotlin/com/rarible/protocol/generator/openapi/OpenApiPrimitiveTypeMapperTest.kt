package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.exception.SchemaValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class OpenApiPrimitiveTypeMapperTest {

    private val mapper = OpenApiPrimitiveTypeMapper(mapOf("integer" to "Int", "integer:int64" to "Long"))

    @Test
    fun `test get definition`() {
        assertEquals("Int", mapper.getDefinition("integer").qualifier)
        assertEquals("Long", mapper.getDefinition("integer", "int64").qualifier)
    }

    @Test
    fun `test definition not found`() {
        assertThrows(SchemaValidationException::class.java) { mapper.getDefinition("double") }
    }

}
