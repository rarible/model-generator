package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeMapper
import com.rarible.protocol.generator.TypeMapperFactory

class OpenApiTypeMapperFactory : TypeMapperFactory {

    override fun getTypeDefinitionMapper(
        qualifierGenerator: QualifierGenerator,
        primitiveTypeMapping: Map<String, String>,
        providedTypeMapping: Map<String, String>
    ): TypeMapper {

        return OpenApiTypeMapper(
            qualifierGenerator,
            OpenApiPrimitiveTypeMapper(primitiveTypeMapping),
            OpenApiProvidedTypeMapper(providedTypeMapping)
        )
    }

}
