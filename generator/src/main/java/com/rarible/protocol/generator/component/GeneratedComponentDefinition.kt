package com.rarible.protocol.generator.component

import com.sun.deploy.util.StringUtils
import java.util.stream.Collectors

class GeneratedComponentDefinition(
    override var name: String?,
    override var packageName: String?
) : ComponentDefinition() {

    var fields: Map<String, FieldDefinition>? = null

    override fun toString(): String {
        return ("${toClassName()} {\n\t"
                + StringUtils.join(fields!!.values.stream()
            .map { obj: FieldDefinition -> obj.toString() }
            .collect(Collectors.toList()), "\n\t") + "\n}\n")
    }
}
