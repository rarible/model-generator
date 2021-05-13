package com.rarible.protocol.generator.type

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream


open class ProvidedTypeStreamReader(
    private val inputStream: InputStream
) : ProvidedTypeReader {

    private val mapper: ObjectMapper = ObjectMapper()
    private val type = mapper.typeFactory.constructMapLikeType(
        HashMap::class.java,
        String::class.java,
        String::class.java
    )

    override fun getMapping(): Map<String, String> {
        return mapper.readValue(inputStream, type)
    }
}
