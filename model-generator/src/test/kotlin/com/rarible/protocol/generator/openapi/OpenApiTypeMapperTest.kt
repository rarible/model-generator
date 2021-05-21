package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.exception.SchemaValidationException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.nio.file.Paths

internal class OpenApiTypeMapperTest {

    private val testSchemasFolder = Paths.get("src/test/resources/schemas")

    @Test
    fun `test provided type is missing`() {
        val mapper = OpenApiTypeMapperFactory().getTypeDefinitionMapper(
            { "test" },
            mapOf("string" to "String"),
            mapOf()
        )
        assertThrows(SchemaValidationException::class.java) {
            mapper.readGeneratedComponents(testSchemasFolder.resolve("test_provided_type_not_found.yaml"))
        }
    }

    @Test
    fun `test ref is missing`() {
        val mapper = OpenApiTypeMapperFactory().getTypeDefinitionMapper(
            { "test" },
            mapOf("string" to "String"),
            mapOf()
        )
        assertThrows(SchemaValidationException::class.java) {
            mapper.readGeneratedComponents(testSchemasFolder.resolve("test_reference_not_found.yaml"))
        }
    }

    @Test
    fun `test enum contains fields`() {
        val mapper = OpenApiTypeMapperFactory().getTypeDefinitionMapper(
            { "test" },
            mapOf("string" to "String"),
            mapOf()
        )
        assertThrows(SchemaValidationException::class.java) {
            mapper.readGeneratedComponents(testSchemasFolder.resolve("test_enum_with_fields.yaml"))
        }
    }

}
