package com.rarible.protocol.generator

import com.rarible.protocol.generator.lang.KotlinGenerator
import com.rarible.protocol.generator.openapi.OpenApiTypeDefinitionMapperFactory
import com.rarible.protocol.generator.type.ProvidedTypeFileReader
import org.junit.jupiter.api.Test
import java.nio.file.Paths

internal class GeneratorTest {

    @Test
    fun test() {
        val primitiveReader = ProvidedTypeFileReader(Paths.get("src/test/resources/lang/kotlin/primitives.json"))
        val providedReader = ProvidedTypeFileReader(Paths.get("src/test/resources/lang/kotlin/provided.json"))
        val templatePath = Paths.get("src/test/resources/lang/kotlin")
        val ymlPath = Paths.get("src/test/resources/schema.yml")
        val outPath = Paths.get("target/generated-sources")

        val generator = KotlinGenerator(
            primitiveReader,
            providedReader,
            OpenApiTypeDefinitionMapperFactory(),
            templatePath,
            "com.rarible.test"
        )
        generator.generate(ymlPath, outPath)

    }
}
