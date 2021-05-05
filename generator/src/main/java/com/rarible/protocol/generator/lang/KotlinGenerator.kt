package com.rarible.protocol.generator.lang

import com.rarible.protocol.generator.AbstractGenerator
import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeDefinitionMapperFactory
import com.rarible.protocol.generator.component.GeneratedComponentDefinition
import com.rarible.protocol.generator.template.ResourceTemplateLoader
import com.rarible.protocol.generator.type.ProvidedTypeReader
import freemarker.template.Configuration
import freemarker.template.TemplateException
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Path


class KotlinGenerator(
    primitiveTypeFileReader: ProvidedTypeReader,
    providedTypeFileReader: ProvidedTypeReader,
    typeDefinitionMapperFactory: TypeDefinitionMapperFactory,
    templateFolder: Path,
    private val packageName: String,
) : AbstractGenerator(primitiveTypeFileReader, providedTypeFileReader, typeDefinitionMapperFactory) {

    private companion object {
        const val SINGLE_TEMPLATE: String = "single_dto"
        const val MULTIPLE_TEMPLATE: String = "multiple_dto"
    }

    private val configuration: Configuration = Configuration(Configuration.VERSION_2_3_23)
    private val qualifierGenerator: QualifierGenerator = QualifierGenerator { "$packageName.$it" }

    init {
        configuration.templateLoader = ResourceTemplateLoader(templateFolder)
        configuration.defaultEncoding = StandardCharsets.UTF_8.displayName()
        configuration.localizedLookup = false
    }

    override fun generate(apiFilePath: Path, outputFolder: Path) {
        val definitions = readDefinitions(apiFilePath)
        val singleDefinitions = LinkedHashSet(definitions)

        val multipleDefinitions = LinkedHashSet<KotlinComponentDefinition>()

        for (definition in definitions) {
            val kotlinDefinition = KotlinComponentDefinition(definition)
            if (kotlinDefinition.isOneOf()) {
                multipleDefinitions.add(kotlinDefinition)
                singleDefinitions.removeAll(definition.oneOf)
                singleDefinitions.remove(definition)
            }
        }

        for (definition in multipleDefinitions) {
            generateMultipleClass(definition, outputFolder)
        }

        for (definition in singleDefinitions) {
            generateSingleClass(KotlinComponentDefinition(definition), outputFolder)
        }
    }

    @Throws(IOException::class, TemplateException::class)
    fun generateSingleClass(definition: KotlinComponentDefinition, outputFolder: Path) {
        val kotlinClass = definition.getKotlinSingleClass()
        val outFile = getOutFile(kotlinClass, outputFolder)
        outFile.writeText(generateSingleClass(kotlinClass), StandardCharsets.UTF_8)
    }

    @Throws(IOException::class, TemplateException::class)
    fun generateMultipleClass(definition: KotlinComponentDefinition, outputFolder: Path) {
        val kotlinClass = definition.getKotlinMultipleClass()
        val outFile = getOutFile(kotlinClass.sealedClass, outputFolder)
        outFile.writeText(generateMultipleClass(kotlinClass), StandardCharsets.UTF_8)
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
                model["oneOf"] = kotlinClass.oneOfMapping
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

    private fun readDefinitions(apiFilePath: Path): List<GeneratedComponentDefinition> {
        val primitiveTypeMapping = primitiveTypeFileReader.getMapping()
        val providedTypeMapping = providedTypeFileReader.getMapping()

        val typeDefinitionMapper = typeDefinitionMapperFactory.getTypeDefinitionMapper(
            qualifierGenerator,
            primitiveTypeMapping,
            providedTypeMapping
        )
        return typeDefinitionMapper.readGeneratedComponents(apiFilePath)
    }


}
