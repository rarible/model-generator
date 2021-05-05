package com.rarible.protocol.generator.lang

data class KotlinMultipleClass(
    val sealedClass: KotlinClass,
    val subclasses: List<KotlinClass>,
    val oneOfMapping: Map<String, String>
)
