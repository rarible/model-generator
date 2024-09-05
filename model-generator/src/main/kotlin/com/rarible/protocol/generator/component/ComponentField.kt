package com.rarible.protocol.generator.component

import org.apache.commons.lang3.StringUtils
import java.util.Locale

data class ComponentField(
    val name: String,
    val type: AbstractComponent,
    val genericTypes: List<AbstractComponent>,
    val enumValues: List<String>,
    val defaultValue: Any?,
    val minimum: Number?,
    val maximum: Number?,
    val pattern: String?
) {
    override fun toString(): String {
        var text = "$name -> ${type.name}(${type.qualifier})"
        if (genericTypes.isNotEmpty()) {
            text = "$text<${StringUtils.join(genericTypes.map { it.name }, ", ")}>"
        }
        if (enumValues.isNotEmpty()) {
            text = "$text[${StringUtils.join(enumValues, ",")}]"
        }
        return text
    }

    val nameCapitalized: String = name.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}
