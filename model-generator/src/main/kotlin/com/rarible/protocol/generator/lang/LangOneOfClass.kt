package com.rarible.protocol.generator.lang

open class LangOneOfClass(
    name: String,
    qualifier: String,
    imports: Set<String>,
    fields: List<LangField>,
    val subclasses: List<LangClass>,
    val discriminatorField: String,
    val oneOfMapping: Map<String, String>,
) : LangClass(name, qualifier, imports, listOf(), fields)
