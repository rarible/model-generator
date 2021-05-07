package com.rarible.protocol.generator.type

import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.file.Path


class ProvidedTypeFileReader(
    private val filePath: Path,
    private val mapper: ObjectMapper = ObjectMapper()
) : ProvidedTypeReader {

    override fun getMapping(): Map<String, String> {
        val type = mapper.typeFactory.constructMapLikeType(
            HashMap::class.java,
            String::class.java,
            String::class.java
        )

        return mapper.readValue(filePath.toFile(), type)
    }
}
