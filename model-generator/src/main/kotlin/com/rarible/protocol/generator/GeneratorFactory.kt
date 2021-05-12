package com.rarible.protocol.generator

import com.rarible.protocol.generator.type.ProvidedTypeReader

interface GeneratorFactory {

    fun getGenerator(
        primitiveTypeFileReader: ProvidedTypeReader,
        providedTypeFileReader: ProvidedTypeReader,
        typeMapperFactory: TypeMapperFactory
    ): Generator

    fun getQualifierGenerator(): QualifierGenerator

    fun getLang(): String

    fun getDefaultPrimitiveTypesReader(): ProvidedTypeReader

    fun getDefaultProvidedTypesReader(): ProvidedTypeReader


}
