package com.rarible.protocol.generator

interface TypeDefinitionMapperFactory {

    fun getTypeDefinitionMapper(
        qualifierGenerator: QualifierGenerator,
        primitiveTypeDefinitions: Map<String, String>,
        providedTypeDefinitions: Map<String, String>
    ): TypeDefinitionMapper

}
