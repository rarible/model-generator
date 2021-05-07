package com.rarible.protocol.generator.type

import com.fasterxml.jackson.databind.ObjectMapper


class ProvidedTypeResourceReader(
    private val filePath: String
) : ProvidedTypeReader {

    private val mapper: ObjectMapper = ObjectMapper()

    override fun getMapping(): Map<String, String> {
        val type = mapper.typeFactory.constructMapLikeType(
            HashMap::class.java,
            String::class.java,
            String::class.java
        )

        return mapper.readValue(javaClass.getResourceAsStream(filePath), type)
    }
}
