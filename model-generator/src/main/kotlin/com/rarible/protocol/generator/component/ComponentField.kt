package com.rarible.protocol.generator.component

data class ComponentField(
    var name: String,
    var type: AbstractComponent,
    var genericTypes: List<AbstractComponent>,
    val enumValues: List<String>,
    var isRequired: Boolean
)
