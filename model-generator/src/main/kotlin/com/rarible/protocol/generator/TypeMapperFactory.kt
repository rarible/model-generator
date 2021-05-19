package com.rarible.protocol.generator

interface TypeMapperFactory {

    fun getTypeDefinitionMapper(
        qualifierGenerator: QualifierGenerator,
        primitiveTypeMapping: Map<String, String>,
        providedTypeMapping: Map<String, String>
    ): TypeMapper

}
