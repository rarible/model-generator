package com.rarible.protocol.generator.component

data class SimpleComponent(
    override val name: String,
    override val qualifier: String
) : AbstractComponent() {

    override fun toString(): String {
        return "$name($qualifier)"
    }

}


