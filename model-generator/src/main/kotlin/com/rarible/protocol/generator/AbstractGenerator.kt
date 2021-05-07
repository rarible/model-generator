package com.rarible.protocol.generator

import com.rarible.protocol.generator.type.ProvidedTypeCascadeReader
import com.rarible.protocol.generator.type.ProvidedTypeConstantReader
import com.rarible.protocol.generator.type.ProvidedTypeReader
import com.rarible.protocol.generator.type.ProvidedTypeResourceReader

abstract class AbstractGenerator(
    primitiveTypeReader: ProvidedTypeReader,
    providedTypeReader: ProvidedTypeReader,
    val typeMapperFactory: TypeMapperFactory
) : Generator {

    val templateFolder: String = "/lang/${getLang()}"

    val primitiveTypeReader: ProvidedTypeReader = ProvidedTypeCascadeReader(
        listOf(
            getDefaultTypeReader("/lang/${getLang()}/primitives.json"),
            primitiveTypeReader
        )
    )

    val providedTypeReader: ProvidedTypeReader = ProvidedTypeCascadeReader(
        listOf(
            getDefaultTypeReader("/lang/${getLang()}/provided.json"),
            providedTypeReader
        )
    )

    protected abstract fun getLang(): String

    private fun getDefaultTypeReader(path: String): ProvidedTypeReader {
        val url = this.javaClass.getResource(path)
        if (url != null) {
            return ProvidedTypeResourceReader(path)
        } else {
            return ProvidedTypeConstantReader(mapOf())
        }
    }
}


