package com.rarible.protocol.generator.lang

data class LangField(
    val name: String,
    val type: String,
    val enum: LangEnum?,
    val required: Boolean,
    val defaultValue: Any?,
    val minimum: Number?,
    val maximum: Number?,
    val pattern: String?,
    var overriden: Boolean = false,
    var abstract: Boolean = false
)
