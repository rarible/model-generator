package com.rarible.protocol.generator

import com.rarible.protocol.generator.type.ProvidedTypeReader

abstract class AbstractGenerator(
    val primitiveTypeReader: ProvidedTypeReader,
    val providedTypeReader: ProvidedTypeReader,
    val typeMapperFactory: TypeMapperFactory
) : Generator

