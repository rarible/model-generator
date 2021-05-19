package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.exception.SchemaValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class OpenApiProvidedTypeMapperTest {

    private val mapper = OpenApiProvidedTypeMapper(mapOf("Address" to "com.test.Address"))

    @Test
    fun `test get definition`() {
        assertEquals("com.test.Address", mapper.getDefinition("Address").qualifier)
    }

    @Test
    fun `test definition not found`() {
        assertThrows(SchemaValidationException::class.java) { mapper.getDefinition("Binary") }
    }
}
