package com.rarible.protocol.generator.lang

import com.rarible.protocol.generator.component.ComponentField
import com.rarible.protocol.generator.component.Discriminator
import com.rarible.protocol.generator.component.GeneratedComponent
import com.rarible.protocol.generator.exception.IllegalOperationException
import org.apache.commons.lang3.StringUtils
import java.util.*

abstract class LangComponent(
    val parent: LangComponent?,
    val definition: GeneratedComponent
) {

    fun isOneOf(): Boolean {
        return definition.discriminator != null
    }

    fun getAllOneOfComponents(): Collection<LangComponent> {
        val result = HashSet<LangComponent>()
        val subcomponents = getOneOfComponents()
        for (subcomponent in subcomponents) {
            result.add(subcomponent)
            if (subcomponent.isOneOf()) {
                result.addAll(subcomponent.getAllOneOfComponents())
            }
        }
        return result
    }

    fun getLangSingleClass(): LangClass {
        return getLangSingleClass(null, listOf())
    }

    private fun getLangSingleClass(oneOfEnum: String?, parentFields: Collection<LangField>): LangClass {
        val allFields = ArrayList(parentFields.map { LangField(it.name, it.type, null, it.required) })
        allFields.addAll(getFields(oneOfEnum))
        return LangClass(
            getName(),
            getQualifier(),
            getImports(),
            getEnumValues(),
            allFields
        )
    }

    fun getLangOneOfClass(): LangOneOfClass {
        return getLangOneOfClass(mapOf())
    }

    private fun getLangOneOfClass(
        parentFields: Map<String, LangField>
    ): LangOneOfClass {
        val discriminatorFieldName = getDiscriminator().fieldName
        val subcomponents = getOneOfComponents()
        val imports = TreeSet(subcomponents.flatMap { it.getImports() })
        imports.addAll(getImports())

        val commonFields = LinkedHashMap(getFields(discriminatorFieldName).associateBy { it.name })
        commonFields.values.forEach { it.abstract = true }
        commonFields.putAll(parentFields)

        val subclasses = ArrayList<LangClass>()
        val oneOfMapping = LinkedHashMap<String, String>()
        for (subcomponent in subcomponents) {
            if (subcomponent.isOneOf()) {
                val langClass = subcomponent.getLangOneOfClass(commonFields)
                subclasses.add(langClass)
                imports.addAll(langClass.imports)
                oneOfMapping.putAll(langClass.oneOfMapping)
            } else {
                val kotlinClass = subcomponent.getLangSingleClass(discriminatorFieldName, commonFields.values)
                applyInheritance(kotlinClass, commonFields.keys)
                oneOfMapping[kotlinClass.simpleClassName] = subcomponent.getOneOfEnum(discriminatorFieldName)
                subclasses.add(kotlinClass)
                imports.addAll(kotlinClass.imports)
            }
        }

        parentFields.forEach { commonFields.remove(it.key) }

        return LangOneOfClass(
            getName(),
            getQualifier(),
            imports,
            ArrayList(commonFields.values),
            subclasses,
            discriminatorFieldName,
            oneOfMapping
        )
    }

    private fun applyInheritance(langClass: LangClass, parentFields: Set<String>) {
        langClass.fields.forEach {
            it.overriden = parentFields.contains(it.name)
        }
        // Not-inherited fields should be last
        Collections.sort(langClass.fields) { f, s -> s.overriden.compareTo(f.overriden) }
    }

    private fun getDiscriminator(): Discriminator {
        if (!isOneOf()) {
            throw IllegalOperationException("Component '${definition.name}' is not a OneOf component")
        }
        return definition.discriminator!!
    }

    private fun getOneOfEnum(discriminatorField: String): String {
        return definition.fields[discriminatorField]!!.enumValues[0]
    }

    private fun getImports(): SortedSet<String> {
        val result = TreeSet<String>()
        for (fieldDefinition in definition.fields.values) {
            result.add(fieldDefinition.type.qualifier)
            for (genericDefinition in fieldDefinition.genericTypes) {
                result.add(genericDefinition.qualifier)
            }
        }
        return result
    }

    // Enums should not be included for oneOf cases
    private fun getFields(discriminatorFieldName: String?): List<LangField> {
        val result = ArrayList<LangField>()
        for (field in definition.fields.values) {
            if (field.name != discriminatorFieldName) {
                var langEnum: LangEnum? = null
                var filedType: String?
                if (field.enumValues.isNotEmpty()) {
                    langEnum = createFieldEnum(field)
                    if (field.type.name == "array") {
                        val collectionType = getSimpleClassName(field.type.qualifier)
                        filedType = "$collectionType<${langEnum.name}>"
                    } else {
                        filedType = langEnum.name
                    }
                } else {
                    filedType = getFieldType(field)
                }

                val kotlinField = LangField(
                    field.name,
                    filedType,
                    langEnum,
                    field.isRequired
                )
                result.add(kotlinField)
            }
        }
        return result
    }

    private fun getFieldType(field: ComponentField): String {
        var result = getSimpleClassName(field.type.qualifier)
        if (field.genericTypes.isNotEmpty()) {
            val genericList = field.genericTypes.map { getSimpleClassName(it.qualifier) }
            val genericString = StringUtils.join(genericList, ", ")
            result = "$result<$genericString>"
        }
        return result
    }

    private fun getOneOfComponents(): Collection<LangComponent> {
        return getDiscriminator().mapping.values.map { fromComponent(this, it) }
    }

    private fun getName(): String {
        return definition.name
    }

    private fun getEnumValues(): List<String> {
        return definition.enums
    }

    fun getQualifier(): String {
        return definition.qualifier
    }

    fun getRootQualifier(): String? {
        var parent = this.parent
        while (parent?.parent != null) {
            parent = parent.parent
        }
        return parent?.definition?.qualifier
    }

    protected abstract fun createFieldEnum(field: ComponentField): LangEnum

    protected abstract fun getSimpleClassName(qualifier: String): String

    protected abstract fun fromComponent(parent: LangComponent, definition: GeneratedComponent): LangComponent

}
