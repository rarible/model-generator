package com.rarible.protocol.generator.lang

open class LangClass(
    val name: String,
    val qualifier: String,
    val imports: Set<String>,
    val enumValues: List<String>,
    val fields: List<LangField>
) {
    val enums: List<LangEnum> = fields.mapNotNull { it.enum }
    val simpleClassName = qualifier.substringAfterLast(".")
    val packageName = qualifier.substringBeforeLast(".")
    open fun hasConstraints(): Boolean =
        fields.any { it.maximum != null || it.minimum != null || !it.pattern.isNullOrEmpty() }
}



