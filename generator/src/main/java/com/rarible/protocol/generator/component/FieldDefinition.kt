package com.rarible.protocol.generator.component

import com.sun.deploy.util.StringUtils
import java.util.stream.Collectors

class FieldDefinition(
    var name: String,
    var type: ComponentDefinition,
    var genericTypes: List<ComponentDefinition>,
    val enumValues: List<String>,
    var isRequired: Boolean
) {

    override fun toString(): String {
        var result = name + ": " + type.qualifier
        if (!genericTypes.isEmpty()) {
            result = "$result<" + printGenerics()
        }
        return "$result (required = $isRequired)"
    }

    private fun printGenerics(): String {
        return StringUtils.join(
            genericTypes.stream()
                .map { obj: ComponentDefinition -> obj.qualifier }
                .collect(Collectors.toList()), ", ") + ">"
    }

}
