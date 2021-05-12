package com.rarible.protocol.generator.lang.kotlin

data class KotlinClass(
    val name: String,
    val packageName: String,
    val imports: Set<String>,
    val fields: List<KotlinField>
) {
    val enums: List<KotlinEnum> = fields.mapNotNull { it.enum }
}
