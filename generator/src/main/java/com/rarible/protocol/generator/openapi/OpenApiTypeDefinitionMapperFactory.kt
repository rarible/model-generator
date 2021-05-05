package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeDefinitionMapper
import com.rarible.protocol.generator.TypeDefinitionMapperFactory

class OpenApiTypeDefinitionMapperFactory : TypeDefinitionMapperFactory {

    override fun getTypeDefinitionMapper(
        qualifierGenerator: QualifierGenerator,
        primitiveTypeDefinitions: Map<String, String>,
        providedTypeDefinitions: Map<String, String>
    ): TypeDefinitionMapper {

        return OpenApiTypeDefinitionMapper(
            qualifierGenerator,
            primitiveTypeDefinitions,
            providedTypeDefinitions
        )
    }
}
