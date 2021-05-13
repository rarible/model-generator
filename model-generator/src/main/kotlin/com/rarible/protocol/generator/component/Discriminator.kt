package com.rarible.protocol.generator.component

data class Discriminator(
    val fieldName: String,
    val mapping: Map<String, GeneratedComponent>,
    val discriminators: Map<String, GeneratedComponent>
)
