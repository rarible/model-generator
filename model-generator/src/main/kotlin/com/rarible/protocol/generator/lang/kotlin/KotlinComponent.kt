package com.rarible.protocol.generator.lang.kotlin

import com.rarible.protocol.generator.component.ComponentField
import com.rarible.protocol.generator.component.Discriminator
import com.rarible.protocol.generator.component.GeneratedComponent
import com.rarible.protocol.generator.exception.IllegalOperationException
import org.apache.commons.lang3.StringUtils
import java.util.*

class KotlinComponent(
    private val definition: GeneratedComponent
) {

    fun isOneOf(): Boolean {
        return definition.discriminator != null
    }

    fun getAllOneOfComponents(): Collection<GeneratedComponent> {
        val result = HashSet<GeneratedComponent>()
        val subcomponents = getOneOfComponents().map { KotlinComponent(it) }
        for (subcomponent in subcomponents) {
            result.add(subcomponent.definition)
            if (subcomponent.isOneOf()) {
                result.addAll(subcomponent.getAllOneOfComponents())
            }
        }
        return result
    }

    fun getKotlinSingleClass(): KotlinClass {
        return getKotlinSingleClass(null, listOf())
    }

    private fun getKotlinSingleClass(oneOfEnum: String?, parentFields: Collection<KotlinField>): KotlinClass {
        val allFields = ArrayList(parentFields.map { KotlinField(it.name, it.type, null, it.required) })
        allFields.addAll(getFields(oneOfEnum))
        return KotlinClass(
            getName(),
            definition.qualifier,
            getImports(),
            getEnumValues(),
            allFields
        )
    }

    fun getKotlinMultipleClass(): KotlinMultipleClass {
        return getKotlinMultipleClass(mapOf())
    }

    private fun getKotlinMultipleClass(
        parentFields: Map<String, KotlinField>
    ): KotlinMultipleClass {
        val discriminatorFieldName = getDiscriminator().fieldName
        val subcomponents = getOneOfComponents().map { KotlinComponent(it) }
        val imports = TreeSet(subcomponents.flatMap { it.getImports() })
        imports.addAll(getImports())

        val commonFields = LinkedHashMap(getFields(discriminatorFieldName).associateBy { it.name })
        commonFields.values.forEach { it.abstract = true }
        commonFields.putAll(parentFields)

        val subclasses = ArrayList<KotlinClass>()
        val oneOfMapping = LinkedHashMap<String, String>()
        for (subcomponent in subcomponents) {
            if (subcomponent.isOneOf()) {
                val kotlinClass = subcomponent.getKotlinMultipleClass(commonFields)
                subclasses.add(kotlinClass)
                imports.addAll(kotlinClass.imports)
                oneOfMapping.putAll(kotlinClass.oneOfMapping)
            } else {
                val kotlinClass = subcomponent.getKotlinSingleClass(discriminatorFieldName, commonFields.values)
                applyInheritance(kotlinClass, commonFields.keys)
                oneOfMapping[kotlinClass.simpleClassName] = subcomponent.getOneOfEnum(discriminatorFieldName)
                subclasses.add(kotlinClass)
                imports.addAll(kotlinClass.imports)
            }
        }

        parentFields.forEach { commonFields.remove(it.key) }

        return KotlinMultipleClass(
            getName(),
            definition.qualifier,
            imports,
            ArrayList(commonFields.values),
            subclasses,
            discriminatorFieldName,
            oneOfMapping
        )
    }

    private fun applyInheritance(kotlinClass: KotlinClass, parentFields: Set<String>) {
        kotlinClass.fields.forEach {
            it.overriden = parentFields.contains(it.name)
        }
        // Not-inherited fields should be last
        Collections.sort(kotlinClass.fields) { f, s -> s.overriden.compareTo(f.overriden) }
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
    private fun getFields(discriminatorFieldName: String?): List<KotlinField> {
        val result = ArrayList<KotlinField>()
        for (field in definition.fields.values) {
            if (!field.name.startsWith("@") && field.name != discriminatorFieldName) {
                var kotlinEnum: KotlinEnum? = null
                var filedType: String?
                if (field.enumValues.isNotEmpty()) {
                    kotlinEnum = KotlinEnum(
                        field.name.capitalize(),
                        field.enumValues
                    )
                    if (field.type.name == "array") {
                        val collectionType = getSimpleClassName(field.type.qualifier)
                        filedType = "$collectionType<${kotlinEnum.name}>"
                    } else {
                        filedType = kotlinEnum.name
                    }
                } else {
                    filedType = getFieldType(field)
                }

                val kotlinField = KotlinField(
                    field.name,
                    filedType,
                    kotlinEnum,
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

    private fun getOneOfComponents(): Collection<GeneratedComponent> {
        return getDiscriminator().mapping.values
    }

    private fun getSimpleClassName(fullClassName: String): String {
        return fullClassName.substringAfterLast('.')
    }

    private fun getName(): String {
        return definition.name
    }

    private fun getEnumValues(): List<String> {
        return definition.enums
    }

}
