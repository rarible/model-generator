package com.rarible.protocol.generator.type

class ProvidedTypeResourceReader(
    filePath: String
) : ProvidedTypeStreamReader(
    ProvidedTypeReader::class.java.getResourceAsStream(filePath)
)
