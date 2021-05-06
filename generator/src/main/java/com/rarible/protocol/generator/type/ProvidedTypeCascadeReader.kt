package com.rarible.protocol.generator.type

class ProvidedTypeCascadeReader(
    private val readers: List<ProvidedTypeReader>
) : ProvidedTypeReader {

    override fun getMapping(): Map<String, String> {
        val result = HashMap<String, String>()
        for (reader in readers) {
            result.putAll(reader.getMapping())
        }
        return result
    }
}
