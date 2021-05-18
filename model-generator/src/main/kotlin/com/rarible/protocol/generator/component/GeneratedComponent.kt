package com.rarible.protocol.generator.component

import org.apache.commons.lang3.StringUtils

data class GeneratedComponent(
    override val name: String,
    override val qualifier: String,
    val fields: Map<String, ComponentField>,
    val discriminator: Discriminator?
) : AbstractComponent() {

    override fun toString(): String {
        var text = "$name($qualifier)"
        if (fields.isNotEmpty()) {
            text = "$text {\n\t${StringUtils.join(fields.values, "\n\t")}\n}"
        }
        if (discriminator != null) {
            text = "$text : discriminator($discriminator)"
        }
        return text
    }
}
