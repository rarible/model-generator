package com.rarible.protocol.generator.lang.ts

import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeMapperFactory
import com.rarible.protocol.generator.component.GeneratedComponent
import com.rarible.protocol.generator.lang.AbstractGenerator
import com.rarible.protocol.generator.lang.ClassWriter
import com.rarible.protocol.generator.lang.LangClass
import com.rarible.protocol.generator.lang.LangOneOfClass
import com.rarible.protocol.generator.type.ProvidedTypeReader
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class TsGenerator(
    lang: String,
    packageName: String,
    primitiveTypesFileReader: ProvidedTypeReader,
    providedTypesFileReader: ProvidedTypeReader,
    typeMapperFactory: TypeMapperFactory,
    qualifierGenerator: QualifierGenerator,
    private val tsGeneratorConfig: TsGeneratorConfig,
) : AbstractGenerator(
    lang,
    packageName,
    typeMapperFactory,
    primitiveTypesFileReader,
    providedTypesFileReader,
    qualifierGenerator
) {

    val primitiveTypes = HashSet(primitiveTypesFileReader.getMapping().values)
    val providedTypes = providedTypesFileReader.getMapping().values.associate {
        Pair(it.substringAfter(":"), it.substringBefore(":"))
    }
    val childParents = HashMap<String, String>()

    override fun generate(apiFilePath: Path, outputFolder: Path, writer: ClassWriter) {
        val rootDefinitions = HashSet(readDefinitions(apiFilePath))
        val leafDefinitions: HashSet<GeneratedComponent> = HashSet()

        for (rootDefinition in rootDefinitions) {
            val tsComponent = TsComponent(null, rootDefinition, tsGeneratorConfig)
            if (tsComponent.isOneOf()) {
                val leafs = tsComponent.getAllOneOfComponents()
                leafs.forEach {
                    leafDefinitions.add(it.definition)
                    if (it.parent != null) {
                        childParents[it.getQualifier()] = it.getRootQualifier()!!
                    }
                }
            }
        }

        rootDefinitions.removeAll(leafDefinitions)

        for (rootDefinition in rootDefinitions) {
            val langComponent = TsComponent(null, rootDefinition, tsGeneratorConfig)
            if (langComponent.isOneOf()) {
                val tsClass = langComponent.getLangOneOfClass()
                val text = generate(MULTIPLE_TEMPLATE, tsClass)
                writer.write(tsClass, text, outputFolder)
            } else {
                val tsClass = langComponent.getLangSingleClass()
                val text = generate(SINGLE_TEMPLATE, tsClass)
                writer.write(tsClass, text, outputFolder)
            }
        }

        writeIndex(outputFolder)
        childParents.clear()
    }

    private fun writeIndex(outputFolder: Path) {
        val outputFolder = outputFolder.resolve(packageName.replace('.', '/') + "/")
        val files = outputFolder.toFile().listFiles()
        // for in-memory writer it is null
        if (files != null) {
            val exports = files.filter {
                it.name != "index.ts"
            }.map {
                "export * from \"./" + it.name.substringBeforeLast(".") + "\";"
            }.toMutableList()

            for ((className, classFile) in providedTypes) {
                exports.add("export {$className} from \"$classFile\";")
            }
            Files.write(outputFolder.resolve("index.ts"), exports)
        }

    }

    override fun getClassFileName(classData: LangClass): String {
        return classData.simpleClassName + ".ts"
    }

    override fun getClassFolder(classData: LangClass): String {
        return packageName.replace('.', '/') + "/"
    }

    override fun toModel(langClass: LangClass): HashMap<String, Any> {
        val model = HashMap<String, Any>()
        model["name"] = langClass.name
        model["enums"] = langClass.enums
        model["enumValues"] = langClass.enumValues
        model["simpleClassName"] = langClass.simpleClassName
        model["package"] = langClass.packageName
        model["imports"] = prepareImports(langClass.imports)
        model["fields"] = langClass.fields
        return model
    }

    override fun toModel(langClass: LangOneOfClass): HashMap<String, Any> {
        val model = toModel(langClass as LangClass)
        model["subclasses"] = langClass.subclasses
        model["discriminatorField"] = langClass.discriminatorField
        model["oneOf"] = langClass.oneOfMapping
        return model
    }

    private fun prepareImports(rawImports: Set<String>): Map<String, String> {

        val result = TreeMap<String, String>()
        for (import in rawImports) {
            // filtering primitive type imports
            if (primitiveTypes.contains(import)) {
                continue
            }
            val fileAndName = import.split(":")
            // provided type with specified file
            if (fileAndName.size == 2) {
                result[fileAndName[1]] = fileAndName[0]
                continue
            }
            // generated type, taking parent file as import or, if there is no parent, name of class itself
            result[import] = "./" + childParents.getOrDefault(import, import)
        }
        return result
    }
}
