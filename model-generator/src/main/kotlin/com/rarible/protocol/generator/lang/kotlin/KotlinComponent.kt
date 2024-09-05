package com.rarible.protocol.generator.lang.kotlin

import com.rarible.protocol.generator.component.ComponentField
import com.rarible.protocol.generator.component.GeneratedComponent
import com.rarible.protocol.generator.lang.LangComponent
import com.rarible.protocol.generator.lang.LangEnum
import com.rarible.protocol.generator.lang.LangField
import org.apache.commons.lang3.StringUtils

class KotlinComponent(
    parent: LangComponent?,
    definition: GeneratedComponent
) : LangComponent(
    parent,
    definition
) {
    override fun createFieldEnum(field: ComponentField): LangEnum {
        return LangEnum(
            field.nameCapitalized,
            field.enumValues
        )
    }

    override fun getSimpleClassName(qualifier: String): String {
        return qualifier.substringAfterLast('.')
    }

    override fun fromComponent(parent: LangComponent, definition: GeneratedComponent): LangComponent {
        return KotlinComponent(parent, definition)
    }

    override fun sanitizeDefaultValue(field: LangField): LangField {
        val type = field.type
        val defaultValue = field.defaultValue

        if (defaultValue == null) {
            if (field.required) {
                return field
            } else {
                return field.copy(defaultValue = "null")
            }
        }
        val result = if (type.startsWith("List<")) {
            val arrayType = StringUtils.substringBetween(type, "<", ">")
            KotlinDefaultValueSanitizer.sanitize(arrayType, defaultValue as List<Any>)
        } else {
            KotlinDefaultValueSanitizer.sanitize(type, defaultValue)
        }
        return field.copy(defaultValue = result)

    }
}