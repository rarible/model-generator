package com.rarible.protocol.generator.lang

import com.rarible.protocol.generator.SchemaValidationException
import com.rarible.protocol.generator.component.FieldDefinition
import com.rarible.protocol.generator.component.GeneratedComponentDefinition
import com.sun.deploy.util.StringUtils
import java.util.*
import java.util.stream.Collectors

class KotlinComponentDefinition(
    private val definition: GeneratedComponentDefinition
) {

    fun isOneOf(): Boolean {
        return definition.oneOf.isNotEmpty()
    }

    fun getKotlinSingleClass(): KotlinClass {
        return KotlinClass(
            getName(),
            getPackage(),
            getImports(),
            getFields()
        )
    }

    fun getKotlinMultipleClass(): KotlinMultipleClass {
        val subcomponents = definition.oneOf.map { KotlinComponentDefinition(it) }
        val imports = TreeSet(subcomponents.flatMap { it.getImports() })

        val parentFields = getCommonFields(subcomponents)
        parentFields.values.forEach { it.abstract = true }

        val subclasses = ArrayList<KotlinClass>()
        val oneOfMapping = LinkedHashMap<String, String>()
        for (subcomponent in subcomponents) {
            val kotlinClass = subcomponent.getKotlinSingleClass()
            kotlinClass.fields.forEach {
                it.overriden = parentFields.contains(it.name)
            }
            // Not-inherited fields should be last
            Collections.sort(kotlinClass.fields) { f, s -> s.overriden.compareTo(f.overriden) }
            oneOfMapping[kotlinClass.name] = subcomponent.getOneOfEnum()
            subclasses.add(kotlinClass)
        }

        val sealedClass = KotlinClass(
            getName(),
            getPackage(),
            imports,
            ArrayList(parentFields.values),
        )

        return KotlinMultipleClass(
            sealedClass,
            subclasses,
            oneOfMapping
        )
    }

    private fun getOneOfEnum(): String {
        return definition.fields["@type"]!!.enumValues[0]
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

    private fun getFields(): List<KotlinField> {
        val result = ArrayList<KotlinField>()
        for (field in definition.fields.values) {
            if (!field.name.startsWith("@")) {
                result.add(KotlinField(field.name, getFieldType(field), field.isRequired))
            }
        }
        return result
    }

    private fun getCommonFields(subcomponents: List<KotlinComponentDefinition>): Map<String, KotlinField> {
        val commonFields = LinkedHashMap<String, KotlinField>()

        val componentFieldNames = ArrayList<Set<String>>()

        // Have no idea how to find common fields in several collections in better way
        var intersectedFields: MutableSet<String> = HashSet()
        for (definition in subcomponents) {
            val fieldNames = HashSet(definition.getFields().map { it.name })
            componentFieldNames.add(fieldNames)
            intersectedFields.addAll(fieldNames)
        }
        for (fieldSet in componentFieldNames) {
            intersectedFields = HashSet(intersectedFields.intersect(fieldSet))
        }

        for (definition in subcomponents) {
            val fields = definition.getFields()
            for (field in fields) {
                if (intersectedFields.contains(field.name)) {
                    val exist = commonFields[field.name]
                    if (exist != null && field != exist) {
                        throw SchemaValidationException(
                            "Common field '${field.name}' type defined differently " +
                                    "in oneOf DTOs: $exist != $field"
                        )
                    }
                    commonFields[field.name] = field
                }
            }
        }
        return commonFields
    }

    private fun getFieldType(field: FieldDefinition): String {
        var result = getSimpleClassName(field.type.qualifier)
        if (field.genericTypes.isNotEmpty()) {
            val genericList = StringUtils.join(field.genericTypes.stream()
                .map { d -> getSimpleClassName(d.qualifier) }
                .collect(Collectors.toList()), ", ")
            result = "$result<$genericList>"
        }
        return result
    }

    private fun getSimpleClassName(fullClassName: String): String {
        return fullClassName.substringAfterLast('.')
    }

    private fun getPackage(): String {
        return definition.qualifier.substringBeforeLast('.')
    }

    private fun getName(): String {
        return definition.name
    }
}
