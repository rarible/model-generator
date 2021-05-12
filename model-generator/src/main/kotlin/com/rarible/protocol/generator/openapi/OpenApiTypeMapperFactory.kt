package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeMapper
import com.rarible.protocol.generator.TypeMapperFactory
import java.io.BufferedWriter
import java.io.File
import java.nio.charset.StandardCharsets

class OpenApiTypeMapperFactory : TypeMapperFactory {

    override fun getTypeDefinitionMapper(
        qualifierGenerator: QualifierGenerator,
        primitiveTypeMapping: Map<String, String>,
        providedTypeMapping: Map<String, String>
    ): TypeMapper {

        return OpenApiTypeMapper(
            qualifierGenerator,
            OpenApiPrimitiveTypeMapper(primitiveTypeMapping),
            OpenApiProvidedTypeMapper(providedTypeMapping)
        )
    }

    override fun mergeSchemas(origin: File, dest: File, additionalTexts: List<String>) {
        val originalLines = origin.readLines()
        BufferedWriter(dest.writer(StandardCharsets.UTF_8)).use { writer ->
            writeLines(writer, originalLines)
            for (text in additionalTexts) {
                writeLines(writer, findComponentsPart(text))
            }
        }
    }

    private fun writeLines(writer: BufferedWriter, lines: List<String>) {
        for (line in lines) {
            writer.write(line)
            writer.newLine()
        }
    }

    private fun findComponentsPart(text: String): List<String> {
        val result = ArrayList<String>()
        val lines = text.split("\n")

        var componentPathFound = false
        for (line in lines) {
            if (componentPathFound) {
                result.add(line.replace("\r", ""))
            } else {
                componentPathFound = "schemas:" == line.trim()
            }
        }
        result.add("")
        return result
    }

}
