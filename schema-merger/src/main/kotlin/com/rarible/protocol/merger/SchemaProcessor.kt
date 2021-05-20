package com.rarible.protocol.merger

fun interface SchemaProcessor {

    companion object {
        val NO_OP = SchemaProcessor { it }
    }

    fun process(text: String): String

}
