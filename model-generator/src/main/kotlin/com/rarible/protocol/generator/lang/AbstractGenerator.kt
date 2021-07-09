package com.rarible.protocol.generator.lang

import com.rarible.protocol.generator.Generator
import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeMapperFactory
import com.rarible.protocol.generator.component.GeneratedComponent
import com.rarible.protocol.generator.template.ResourceTemplateLoader
import com.rarible.protocol.generator.type.ProvidedTypeReader
import freemarker.template.Configuration
import freemarker.template.TemplateException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths

abstract class AbstractGenerator(
    val lang: String,
    val packageName: String,
    val typeMapperFactory: TypeMapperFactory,
    private val primitiveTypesFileReader: ProvidedTypeReader,
    private val providedTypesFileReader: ProvidedTypeReader,
    private val qualifierGenerator: QualifierGenerator
) : Generator {

    val templateFolder: String = "/model-generator/$lang"

    companion object {
        const val SINGLE_TEMPLATE: String = "single_dto"
        const val MULTIPLE_TEMPLATE: String = "multiple_dto"
    }

    private val configuration: Configuration = Configuration(Configuration.VERSION_2_3_23)

    private val fileWriter: ClassWriter = ClassWriter { c, t, o -> write(c, t, o) }

    init {
        configuration.templateLoader = ResourceTemplateLoader(templateFolder)
        configuration.defaultEncoding = StandardCharsets.UTF_8.displayName()
        configuration.localizedLookup = false
    }

    override fun generate(apiFilePath: Path): Map<String, String> {
        val writer = InMemoryClassWriter(LinkedHashMap())
        // Second argument doesn't affect anything
        generate(apiFilePath, Paths.get("/"), writer)
        return writer.output
    }

    override fun generate(apiFilePath: Path, outputFolder: Path) {
        generate(apiFilePath, outputFolder, fileWriter)
    }

    abstract fun generate(apiFilePath: Path, outputFolder: Path, writer: ClassWriter)

    @Throws(IOException::class, TemplateException::class)
    fun generate(template: String, kotlinClass: LangClass): String {
        return generate(template, toModel(kotlinClass))
    }

    @Throws(IOException::class, TemplateException::class)
    fun generate(template: String, langClass: LangOneOfClass): String {
        return generate(template, toModel(langClass))
    }

    @Throws(IOException::class, TemplateException::class)
    private fun generate(template: String, model: Map<String, Any>): String {
        ByteArrayOutputStream().use { out ->
            OutputStreamWriter(out).use { writer ->
                configuration.getTemplate(template).process(model, writer)
                writer.flush()
            }
            return out.toString()
        }
    }

    fun readDefinitions(apiFilePath: Path): List<GeneratedComponent> {
        val primitiveTypeMapping = primitiveTypesFileReader.getMapping()
        val providedTypeMapping = providedTypesFileReader.getMapping()

        val typeDefinitionMapper = typeMapperFactory.getTypeDefinitionMapper(
            qualifierGenerator,
            primitiveTypeMapping,
            providedTypeMapping
        )
        return typeDefinitionMapper.readGeneratedComponents(apiFilePath)
    }

    fun write(classData: LangClass, text: String, outputFolder: Path) {
        val outFile = getOutFile(classData, outputFolder)
        outFile.writeText(text, StandardCharsets.UTF_8)
    }

    fun getOutFile(classData: LangClass, outputFolder: Path): File {
        val packageFolder = getClassFolder(classData)
        val classFileRelativePath = packageFolder + getClassFileName(classData)
        val outFile = outputFolder.resolve(classFileRelativePath).toFile()
        outFile.parentFile.mkdirs()
        return outFile
    }

    abstract fun toModel(classData: LangClass): HashMap<String, Any>

    abstract fun toModel(classData: LangOneOfClass): HashMap<String, Any>

    abstract fun getClassFileName(classData: LangClass): String

    abstract fun getClassFolder(classData: LangClass): String


}