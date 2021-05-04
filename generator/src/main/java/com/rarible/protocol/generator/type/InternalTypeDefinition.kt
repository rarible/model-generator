package com.rarible.protocol.generator.type

class InternalTypeDefinition(
    val openApiType: String,
    val javaClass: Class<*>
) {

    companion object {
        fun of(openApiType: String, javaType: Class<*>): InternalTypeDefinition {
            return InternalTypeDefinition(openApiType, javaType)
        }
    }
}
