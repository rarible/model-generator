package com.rarible.protocol.generator.component

import com.sun.deploy.util.StringUtils
import java.util.stream.Collectors

class GeneratedComponentDefinition(
    override val name: String,
    override val qualifier: String,
    val fields: Map<String, FieldDefinition>,
    val oneOf: List<GeneratedComponentDefinition>
) : ComponentDefinition() {

    override fun toString(): String {
        return "$qualifier {\n\t" +
                StringUtils.join(fields.values.stream()
                    .map { d -> d.toString() }
                    .collect(Collectors.toList()), "\n\t") +
                StringUtils.join(oneOf.stream()
                    .map { c -> c.qualifier + " -> " + c.fields["@type"]!!.enumValues }
                    .collect(Collectors.toList()), "\n\t") + "\n}\n"
    }
}
