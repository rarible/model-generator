package com.rarible.protocol.generator

import com.rarible.protocol.generator.type.ProvidedTypeCascadeReader
import com.rarible.protocol.generator.type.ProvidedTypeConstantReader
import com.rarible.protocol.generator.type.ProvidedTypeReader
import com.rarible.protocol.generator.type.ProvidedTypeResourceReader

abstract class AbstractGeneratorFactory : GeneratorFactory {

    private val primitiveTypeReader: ProvidedTypeReader =
        getDefaultTypeReader("/model-generator/${getLang()}/primitives.json")

    private val providedTypeReader: ProvidedTypeReader =
        getDefaultTypeReader("/model-generator/${getLang()}/provided.json")

    override fun getDefaultPrimitiveTypesReader(): ProvidedTypeReader {
        return primitiveTypeReader
    }

    override fun getDefaultProvidedTypesReader(): ProvidedTypeReader {
        return providedTypeReader
    }

    override fun getGenerator(
        primitiveTypesFileReader: ProvidedTypeReader,
        providedTypesFileReader: ProvidedTypeReader,
        typeMapperFactory: TypeMapperFactory
    ): Generator {
        val primitiveTypesCascadeReader = ProvidedTypeCascadeReader(
            listOf(
                getDefaultPrimitiveTypesReader(),
                primitiveTypesFileReader
            )
        )
        val providedTypesCascadeReader = ProvidedTypeCascadeReader(
            listOf(
                getDefaultProvidedTypesReader(),
                providedTypesFileReader
            )
        )
        return getGeneratorWithDefaultTypes(primitiveTypesCascadeReader, providedTypesCascadeReader, typeMapperFactory)
    }

    abstract fun getGeneratorWithDefaultTypes(
        primitiveTypeFileReader: ProvidedTypeReader,
        providedTypeFileReader: ProvidedTypeReader,
        typeMapperFactory: TypeMapperFactory
    ): Generator

    private fun getDefaultTypeReader(path: String): ProvidedTypeReader {
        val url = this.javaClass.getResource(path)
        if (url != null) {
            return ProvidedTypeResourceReader(path)
        } else {
            return ProvidedTypeConstantReader(mapOf())
        }
    }
}
