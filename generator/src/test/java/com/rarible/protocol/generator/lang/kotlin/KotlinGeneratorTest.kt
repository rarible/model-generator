package com.rarible.protocol.generator.lang.kotlin

import com.rarible.protocol.generator.openapi.OpenApiTypeMapperFactory
import com.rarible.protocol.generator.type.ProvidedTypeFileReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.max

internal class KotlinGeneratorTest {

    private val kotlinResourcesFolder = Paths.get("src/main/resources/lang/kotlin")
    private val primitiveReader = ProvidedTypeFileReader(kotlinResourcesFolder.resolve("primitives.json"))
    private val providedReader = ProvidedTypeFileReader(kotlinResourcesFolder.resolve("provided.json"))

    private val testSchemasFolder = Paths.get("src/test/resources/schemas")
    private val expectedClassesFolder = Paths.get("src/test/resources/kotlin/expected")

    private val outPath = Paths.get("target/generated-sources")

    private val withInheritance: KotlinGenerator = createDefaultGenerator(true)
    private val withoutInheritance: KotlinGenerator = createDefaultGenerator(false)

    // For manual testing only
    @Test
    fun generateFiles() {
//        generateAsFiles("test_single_primitives.yaml")
//        generateAsFiles("test_single_several_classes.yaml")
//        generateAsFiles("test_single_provided_types.yaml")
//        generateAsFiles("test_mixed.yaml")
//        generateAsFiles("test_multiple_with_discriminator.yaml")
//        generateAsFiles("test_multiple_without_inheritance.yaml", withoutInheritance)
    }

    @Test
    fun `test class with primitive fields`() {
        verifyGeneratedClasses(generateAsStrings("test_simple.yaml"))
    }

    @Test
    fun `test single class with several enums`() {
        verifyGeneratedClasses(generateAsStrings("test_enums_single.yaml"))
    }

    @Test
    fun `test single class with enums and arrays of enums`() {
        verifyGeneratedClasses(generateAsStrings("test_enum_arrays_single.yaml"))
    }

    @Test
    fun `test several single classes with basic types`() {
        verifyGeneratedClasses(generateAsStrings("test_several_single_classes.yaml"))
    }

    @Test
    fun `test single class with provided type`() {
        verifyGeneratedClasses(generateAsStrings("test_provided_types.yaml"))
    }

    @Test
    fun `test mixed classes with oneOf and enums`() {
        verifyGeneratedClasses(generateAsStrings("test_mixed_types.yaml"))
    }

    @Test
    fun `test oneOf with discriminator`() {
        verifyGeneratedClasses(generateAsStrings("test_one_of_with_discriminator.yaml"))
    }

    private fun verifyGeneratedClasses(classes: Map<String, String>) {
        for ((name, text) in classes) {
            val expected = readExpectedClassText(name).filter { it.trim().isNotEmpty() }
            val original = text.split("\n").filter { it.trim().isNotEmpty() }

            for (i in 0..max(expected.size, original.size)) {
                val expectedLine = if (i < expected.size) expected[i].trim() else ""
                val originalLine = if (i < original.size) original[i].trim() else ""
                assertEquals(expectedLine, originalLine, "Validation of class '$name' failed at line $i:")
            }
        }
    }

    private fun readExpectedClassText(className: String): List<String> {
        return Files.readAllLines(expectedClassesFolder.resolve("$className.txt"))
    }

    private fun generateAsStrings(yamlFileName: String): Map<String, String> {
        val generator = createDefaultGenerator()
        val ymlPath = testSchemasFolder.resolve(yamlFileName)
        return generator.generate(ymlPath)
    }

    private fun generateAsFiles(yamlFileName: String) {
        val generator = createDefaultGenerator()
        val ymlPath = testSchemasFolder.resolve(yamlFileName)
        generator.generate(ymlPath, outPath)
    }

    private fun createDefaultGenerator(): KotlinGenerator {
        return KotlinGenerator(
            primitiveReader,
            providedReader,
            OpenApiTypeMapperFactory(),
            kotlinResourcesFolder,
            "com.rarible.test"
        )
    }
}
