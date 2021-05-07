package com.rarible.protocol.generator.lang.kotlin

data class KotlinField(
    val name: String,
    val type: String,
    val enum: KotlinEnum?,
    val required: Boolean,
    var overriden: Boolean = false,
    var abstract: Boolean = false
)
