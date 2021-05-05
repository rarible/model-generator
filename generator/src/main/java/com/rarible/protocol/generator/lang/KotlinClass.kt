package com.rarible.protocol.generator.lang

data class KotlinClass(
    val name: String,
    val packageName: String,
    val imports: Set<String>,
    val fields: List<KotlinField>
)
