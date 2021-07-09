package com.rarible.protocol.generator.lang

data class LangField(
    val name: String,
    val type: String,
    val enum: LangEnum?,
    val required: Boolean,
    var overriden: Boolean = false,
    var abstract: Boolean = false
)
