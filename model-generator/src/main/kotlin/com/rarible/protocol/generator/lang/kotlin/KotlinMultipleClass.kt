package com.rarible.protocol.generator.lang.kotlin

class KotlinMultipleClass(
    name: String,
    packageName: String,
    imports: Set<String>,
    fields: List<KotlinField>,
    val subclasses: List<KotlinClass>,
    val discriminatorField: String,
    val oneOfMapping: Map<String, String>,
) : KotlinClass(name, packageName, imports, fields) {

}
