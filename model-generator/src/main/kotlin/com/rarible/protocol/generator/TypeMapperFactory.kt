package com.rarible.protocol.generator

import java.io.File

interface TypeMapperFactory {

    fun getTypeDefinitionMapper(
        qualifierGenerator: QualifierGenerator,
        primitiveTypeMapping: Map<String, String>,
        providedTypeMapping: Map<String, String>
    ): TypeMapper

    fun mergeSchemas(origin: File, dest: File, additionalTexts: List<String>)
}
