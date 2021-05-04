package com.rarible.protocol.generator.type

class ExternalTypeDefinition private constructor(
    val componentName: String,
    val javaClass: Class<*>
) {

    companion object {
        fun of(componentName: String, javaClass: Class<*>): ExternalTypeDefinition {
            return ExternalTypeDefinition(componentName, javaClass)
        }
    }
}
