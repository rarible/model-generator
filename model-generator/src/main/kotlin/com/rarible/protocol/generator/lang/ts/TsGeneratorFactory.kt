package com.rarible.protocol.generator.lang.ts

import com.rarible.protocol.generator.Generator
import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeMapperFactory
import com.rarible.protocol.generator.lang.AbstractGeneratorFactory
import com.rarible.protocol.generator.type.ProvidedTypeReader

class TsGeneratorFactory(
    private val packageName: String,
    private val enumSuffix: String,
) : AbstractGeneratorFactory() {

    private val qualifierGenerator: QualifierGenerator = QualifierGenerator { name, isEnum ->
        if (!isEnum) name else name + enumSuffix
    }

    override fun getGeneratorWithDefaultTypes(
        primitiveTypeFileReader: ProvidedTypeReader,
        providedTypeFileReader: ProvidedTypeReader,
        typeMapperFactory: TypeMapperFactory
    ): Generator {
        return TsGenerator(
            getLang(),
            packageName,
            primitiveTypeFileReader,
            providedTypeFileReader,
            typeMapperFactory,
            getQualifierGenerator(),
            TsGeneratorConfig(enumSuffix = enumSuffix),
        )
    }

    override fun getLang(): String {
        return "ts"
    }

    override fun getQualifierGenerator(): QualifierGenerator {
        return qualifierGenerator
    }
}
