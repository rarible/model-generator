package com.rarible.protocol.generator

import com.rarible.protocol.generator.type.ProvidedTypeReader

abstract class AbstractGenerator(
    val primitiveTypeFileReader: ProvidedTypeReader,
    val providedTypeFileReader: ProvidedTypeReader,
    val typeDefinitionMapperFactory: TypeDefinitionMapperFactory
) : Generator

