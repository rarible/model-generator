package com.rarible.protocol.generator.lang.kotlin

data class KotlinMultipleClass(
    val sealedClass: KotlinClass,
    val subclasses: List<KotlinClass>,
    val discriminatorField: String,
    val oneOfMapping: Map<String, String>
) {
    val enums: List<KotlinEnum> = subclasses.flatMap { it.enums }
}
