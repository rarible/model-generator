package com.rarible.protocol.generator.lang.kotlin

class KotlinMultipleClass(
    name: String,
    qualifier: String,
    imports: Set<String>,
    fields: List<KotlinField>,
    val subclasses: List<KotlinClass>,
    val discriminatorField: String,
    val oneOfMapping: Map<String, String>,
) : KotlinClass(name, qualifier, imports, listOf(), fields)
