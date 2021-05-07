package com.rarible.protocol.generator.lang.kotlin

import com.rarible.protocol.generator.AbstractGenerator
import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeMapperFactory
import com.rarible.protocol.generator.component.GeneratedComponent
import com.rarible.protocol.generator.lang.kotlin.KotlinGenerator.ClassWriter
import com.rarible.protocol.generator.template.ResourceTemplateLoader
import com.rarible.protocol.generator.type.ProvidedTypeReader
import freemarker.template.Configuration
import freemarker.template.TemplateException
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths


class KotlinGenerator(
    primitiveTypeFileReader: ProvidedTypeReader,
    providedTypeFileReader: ProvidedTypeReader,
    typeMapperFactory: TypeMapperFactory,
    private val packageName: String,
    private val withInheritance: Boolean
) : AbstractGenerator(primitiveTypeFileReader, providedTypeFileReader, typeMapperFactory) {

    private companion object {
        const val SINGLE_TEMPLATE: String = "single_dto"
        const val MULTIPLE_TEMPLATE: String = "multiple_dto"
    }

    private val configuration: Configuration = Configuration(Configuration.VERSION_2_3_23)
    private val qualifierGenerator: QualifierGenerator = QualifierGenerator { "$packageName.$it" }

    private val fileWriter: ClassWriter = ClassWriter { c, t, o -> write(c, t, o) }

    init {
        configuration.templateLoader = ResourceTemplateLoader(templateFolder)
        configuration.defaultEncoding = StandardCharsets.UTF_8.displayName()
        configuration.localizedLookup = false
    }

    override fun getLang(): String {
        return "kotlin"
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

    private fun generate(apiFilePath: Path, outputFolder: Path, writer: ClassWriter) {
        val definitions = readDefinitions(apiFilePath)
        val singleDefinitions = LinkedHashSet(definitions)

        val multipleDefinitions = LinkedHashSet<KotlinComponent>()

        for (definition in definitions) {
            val kotlinDefinition = KotlinComponent(definition)
            if (kotlinDefinition.isOneOf()) {
                multipleDefinitions.add(kotlinDefinition)
                singleDefinitions.removeAll(kotlinDefinition.getOneOfComponents())
                singleDefinitions.remove(definition)
            }
        }

        for (definition in multipleDefinitions) {
            val kotlinClass = definition.getKotlinMultipleClass(withInheritance)
            val text = generateMultipleClass(kotlinClass)
            writer.write(kotlinClass.sealedClass, text, outputFolder)
        }

        for (definition in singleDefinitions) {
            val kotlinDefinition = KotlinComponent(definition)
            val kotlinClass = kotlinDefinition.getKotlinSingleClass()
            val text = generateSingleClass(kotlinClass)
            writer.write(kotlinClass, text, outputFolder)
        }
    }

    private fun write(kotlinClass: KotlinClass, text: String, outputFolder: Path) {
        val outFile = getOutFile(kotlinClass, outputFolder)
        outFile.writeText(text, StandardCharsets.UTF_8)
    }

    private fun getOutFile(kotlinClass: KotlinClass, outputFolder: Path): File {
        val packageFolder = kotlinClass.packageName.replace('.', '/') + "/"
        val classFileRelativePath = packageFolder + kotlinClass.name + ".kt"
        val outFile = outputFolder.resolve(classFileRelativePath).toFile()
        outFile.parentFile.mkdirs()
        return outFile
    }

    @Throws(IOException::class, TemplateException::class)
    private fun generateSingleClass(kotlinClass: KotlinClass): String {
        ByteArrayOutputStream().use { out ->
            OutputStreamWriter(out).use { writer ->
                val model = HashMap<String, Any>()
                fillKotlinClassModel(model, kotlinClass)
                model["enums"] = kotlinClass.enums
                generate(writer, SINGLE_TEMPLATE, model)
                writer.flush()
            }
            return out.toString()
        }
    }

    @Throws(IOException::class, TemplateException::class)
    private fun generateMultipleClass(kotlinClass: KotlinMultipleClass): String {
        ByteArrayOutputStream().use { out ->
            OutputStreamWriter(out).use { writer ->
                val model = HashMap<String, Any>()
                fillKotlinClassModel(model, kotlinClass.sealedClass)
                model["subclasses"] = kotlinClass.subclasses
                model["discriminatorField"] = kotlinClass.discriminatorField
                model["oneOf"] = kotlinClass.oneOfMapping
                model["enums"] = kotlinClass.enums
                generate(writer, MULTIPLE_TEMPLATE, model)
                writer.flush()
            }
            return out.toString()
        }
    }

    @Throws(IOException::class, TemplateException::class)
    private fun generate(writer: Writer, template: String, model: Map<String, Any>) {
        configuration.getTemplate(template).process(model, writer)
    }

    private fun fillKotlinClassModel(model: HashMap<String, Any>, kotlinClass: KotlinClass) {
        model["name"] = kotlinClass.name
        model["package"] = kotlinClass.packageName
        model["imports"] = kotlinClass.imports
        model["fields"] = kotlinClass.fields
    }

    private fun readDefinitions(apiFilePath: Path): List<GeneratedComponent> {
        val primitiveTypeMapping = primitiveTypeReader.getMapping()
        val providedTypeMapping = providedTypeReader.getMapping()

        val typeDefinitionMapper = typeMapperFactory.getTypeDefinitionMapper(
            qualifierGenerator,
            primitiveTypeMapping,
            providedTypeMapping
        )
        return typeDefinitionMapper.readGeneratedComponents(apiFilePath)
    }

    private fun interface ClassWriter {
        fun write(kotlinClass: KotlinClass, text: String, outputFolder: Path)
    }

    private class InMemoryClassWriter(val output: MutableMap<String, String>) : ClassWriter {
        override fun write(kotlinClass: KotlinClass, text: String, outputFolder: Path) {
            output[kotlinClass.name] = text
        }
    }
}
