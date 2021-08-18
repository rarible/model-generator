package com.rarible.protocol.generator.lang

data class LangField(
    val name: String,
    val type: String,
    val enum: LangEnum?,
    val required: Boolean,
    val defaultValue: Any?,
    var overriden: Boolean = false,
    var abstract: Boolean = false
)
