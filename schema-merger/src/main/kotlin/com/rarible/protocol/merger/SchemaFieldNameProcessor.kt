package com.rarible.protocol.merger

fun interface SchemaFieldNameProcessor {

    companion object {
        val NO_OP = SchemaFieldNameProcessor { it }
    }

    fun process(originalPath: String): String

}
