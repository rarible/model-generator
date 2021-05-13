package com.rarible.protocol.generator.type

class ProvidedTypeConstantReader(
    private val types: Map<String, String>
) : ProvidedTypeReader {

    override fun getMapping(): Map<String, String> {
        return types
    }
}
