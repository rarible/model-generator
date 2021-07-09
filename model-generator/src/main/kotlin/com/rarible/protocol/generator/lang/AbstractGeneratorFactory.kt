package com.rarible.protocol.generator.lang

import com.rarible.protocol.generator.Generator
import com.rarible.protocol.generator.GeneratorFactory
import com.rarible.protocol.generator.TypeMapperFactory
import com.rarible.protocol.generator.type.ProvidedTypeCascadeReader
import com.rarible.protocol.generator.type.ProvidedTypeReader
import com.rarible.protocol.generator.type.ProvidedTypeResourceReader

abstract class AbstractGeneratorFactory : GeneratorFactory {

    private val primitiveTypeReader: ProvidedTypeReader =
        ProvidedTypeResourceReader("/model-generator/${getLang()}/primitives.json")

    private val providedTypeReader: ProvidedTypeReader =
        ProvidedTypeResourceReader("/model-generator/${getLang()}/provided.json")

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
}
