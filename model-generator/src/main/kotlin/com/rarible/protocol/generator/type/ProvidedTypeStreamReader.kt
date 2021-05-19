package com.rarible.protocol.generator.type

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream


open class ProvidedTypeStreamReader(
    inputStream: InputStream
) : ProvidedTypeReader {

    private val mapper: ObjectMapper = ObjectMapper()
    private val type = mapper.typeFactory.constructMapLikeType(
        HashMap::class.java,
        String::class.java,
        String::class.java
    )

    private val mapping: Map<String, String> = mapper.readValue(inputStream, type)

    override fun getMapping(): Map<String, String> {
        return mapping
    }
}
