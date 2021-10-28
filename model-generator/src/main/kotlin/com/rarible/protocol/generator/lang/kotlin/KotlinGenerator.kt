package com.rarible.protocol.generator.lang.kotlin

import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeMapperFactory
import com.rarible.protocol.generator.component.GeneratedComponent
import com.rarible.protocol.generator.lang.AbstractGenerator
import com.rarible.protocol.generator.lang.ClassWriter
import com.rarible.protocol.generator.lang.LangClass
import com.rarible.protocol.generator.lang.LangOneOfClass
import com.rarible.protocol.generator.type.ProvidedTypeReader
import java.nio.file.Path


class KotlinGenerator(
    lang: String,
    packageName: String,
    primitiveTypesFileReader: ProvidedTypeReader,
    providedTypesFileReader: ProvidedTypeReader,
    typeMapperFactory: TypeMapperFactory,
    qualifierGenerator: QualifierGenerator
) : AbstractGenerator(
    lang,
    packageName,
    typeMapperFactory,
    primitiveTypesFileReader,
    providedTypesFileReader,
    qualifierGenerator
) {

    override fun generate(apiFilePath: Path, outputFolder: Path, writer: ClassWriter) {
        val rootDefinitions = HashSet(readDefinitions(apiFilePath))
        val leafDefinitions: HashSet<GeneratedComponent> = HashSet()

        for (rootDefinition in rootDefinitions) {
            val kotlinComponent = KotlinComponent(null, rootDefinition)
            if (kotlinComponent.isOneOf()) {
                leafDefinitions.addAll(kotlinComponent.getAllOneOfComponents().map { it.definition })
            }
        }

        rootDefinitions.removeAll(leafDefinitions)

        for (rootDefinition in rootDefinitions) {
            val kotlinComponent = KotlinComponent(null, rootDefinition)
            if (kotlinComponent.isOneOf()) {
                val kotlinClass = kotlinComponent.getLangOneOfClass()
                val text = generate(MULTIPLE_TEMPLATE, kotlinClass)
                writer.write(kotlinClass, text, outputFolder)
            } else {
                val kotlinClass = kotlinComponent.getLangSingleClass()
                val text = generate(SINGLE_TEMPLATE, kotlinClass)
                writer.write(kotlinClass, text, outputFolder)
            }
        }
    }

    override fun getClassFileName(classData: LangClass): String {
        return classData.simpleClassName + ".kt"
    }

    override fun getClassFolder(classData: LangClass): String {
        return packageName.replace('.', '/') + "/"
    }

    override fun toModel(classData: LangClass): HashMap<String, Any> {
        val model = HashMap<String, Any>()
        model["name"] = classData.name
        model["enums"] = classData.enums
        model["enumValues"] = classData.enumValues
        model["simpleClassName"] = classData.simpleClassName
        model["package"] = classData.packageName
        model["imports"] = classData.imports
        model["fields"] = classData.fields
        model["hasConstraints"] = classData.hasConstraints
        return model
    }

    override fun toModel(classData: LangOneOfClass): HashMap<String, Any> {
        val model = toModel(classData as LangClass)
        model["subclasses"] = classData.subclasses
        model["discriminatorField"] = classData.discriminatorField
        model["oneOf"] = classData.oneOfMapping
        return model
    }
}
