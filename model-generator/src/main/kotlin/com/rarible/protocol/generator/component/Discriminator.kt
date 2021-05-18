package com.rarible.protocol.generator.component

import org.apache.commons.lang3.StringUtils

data class Discriminator(
    val fieldName: String,
    val mapping: Map<String, GeneratedComponent>
) {
    override fun toString(): String {
        return "$fieldName -> {${StringUtils.join(mapping.values.map { it.name }, ", ")}}"
    }
}
