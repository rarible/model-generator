package com.rarible.protocol.generator.lang

data class KotlinField(
    val name: String,
    val type: String,
    val required: Boolean,
    var overriden: Boolean = false,
    var abstract: Boolean = false
)
