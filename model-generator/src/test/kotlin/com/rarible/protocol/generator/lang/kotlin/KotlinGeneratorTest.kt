import com.rarible.protocol.generator.Generator
import com.rarible.protocol.generator.lang.kotlin.KotlinGenerator
import com.rarible.protocol.generator.lang.kotlin.KotlinGeneratorFactory
import com.rarible.protocol.generator.openapi.OpenApiTypeMapperFactory
import com.rarible.protocol.generator.type.ProvidedTypeConstantReader
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.max

internal class KotlinGeneratorTest {

    // No additional mappings provided, only default used
    private val primitiveReader = ProvidedTypeConstantReader(mapOf())
    private val providedReader = ProvidedTypeConstantReader(mapOf())

    private val testSchemasFolder = Paths.get("src/test/resources/kotlin/schemas")
    private val expectedClassesFolder = Paths.get("src/test/resources/kotlin/expected")

    private val outPath = Paths.get("target/generated-sources")

    private val generator: KotlinGenerator = createDefaultGenerator()

    // For manual testing only
    //@Test
    fun generateFiles() {
        generateAsFiles("test_single_primitives.yaml", generator)
        generateAsFiles("test_single_several_classes.yaml", generator)
        generateAsFiles("test_single_provided_types.yaml", generator)
        generateAsFiles("test_multiple_with_discriminator.yaml", generator)
        generateAsFiles("test_refs.yaml", generator)
        generateAsFiles("test_mixed.yaml", generator)
        generateAsFiles("test_oneof_as_field.yaml", generator)
        generateAsFiles("test_inner_oneof.yaml", generator)
        generateAsFiles("test_enum_ref.yaml", generator)
    }

    @Test
    fun `test lang`() {
        assertEquals("kotlin", generator.lang)
    }

    @Test
    fun `test referenced enums`() {
        verifyGeneratedClasses(generateAsStrings("test_enum_ref.yaml", generator))
    }

    @Test
    fun `test single class with primitive fields`() {
        verifyGeneratedClasses(generateAsStrings("test_single_primitives.yaml", generator))
    }

    @Test
    fun `test single classes with basic types`() {
        verifyGeneratedClasses(generateAsStrings("test_single_several_classes.yaml", generator))
    }

    @Test
    fun `test single class with provided type`() {
        verifyGeneratedClasses(generateAsStrings("test_single_provided_types.yaml", generator))
    }

    @Test
    fun `test oneOf with discriminator`() {
        verifyGeneratedClasses(generateAsStrings("test_multiple_with_discriminator.yaml", generator))
    }

    @Test
    fun `test class with inner refs as fields and map`() {
        verifyGeneratedClasses(generateAsStrings("test_refs.yaml", generator))
    }

    @Test
    fun `test mixed classes with oneOf and enums`() {
        verifyGeneratedClasses(generateAsStrings("test_mixed.yaml", generator))
    }

    @Test
    fun `test oneOf used as field of another class`() {
        verifyGeneratedClasses(generateAsStrings("test_oneof_as_field.yaml", generator))
    }

    @Test
    fun `test oneOf with inner oneOf`() {
        verifyGeneratedClasses(generateAsStrings("test_inner_oneof.yaml", generator))
    }

    private fun verifyGeneratedClasses(classes: Map<String, String>) {
        for ((name, text) in classes) {
            val expected = readExpectedClassText(name).filter { it.trim().isNotEmpty() }
            val original = text.split("\n").filter { it.trim().isNotEmpty() }

            for (i in 0..max(expected.size, original.size)) {
                val expectedLine = if (i < expected.size) expected[i].trim() else ""
                val originalLine = if (i < original.size) original[i].trim() else ""
                Assertions.assertEquals(expectedLine, originalLine, "Validation of class '$name' failed at line $i:")
            }
        }
    }

    private fun readExpectedClassText(className: String): List<String> {
        return Files.readAllLines(expectedClassesFolder.resolve("$className.txt"))
    }

    private fun generateAsStrings(yamlFileName: String, generator: Generator): Map<String, String> {
        val ymlPath = testSchemasFolder.resolve(yamlFileName)
        return generator.generate(ymlPath)
    }

    private fun generateAsFiles(yamlFileName: String, generator: Generator) {
        val ymlPath = testSchemasFolder.resolve(yamlFileName)
        generator.generate(ymlPath, outPath)
    }

    private fun createDefaultGenerator(): KotlinGenerator {
        val factory = KotlinGeneratorFactory(
            "com.rarible.test"
        )

        return factory.getGenerator(primitiveReader, providedReader, OpenApiTypeMapperFactory()) as KotlinGenerator
    }
}
