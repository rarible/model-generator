package com.rarible.protocol.generator.lang.kotlin

open class KotlinClass(
    val name: String,
    qualifier: String,
    val imports: Set<String>,
    val enumValues: List<String>,
    val fields: List<KotlinField>
) {
    val enums: List<KotlinEnum> = fields.mapNotNull { it.enum }
    val simpleClassName = qualifier.substringAfterLast(".")
    val packageName = qualifier.substringBeforeLast(".")

}

