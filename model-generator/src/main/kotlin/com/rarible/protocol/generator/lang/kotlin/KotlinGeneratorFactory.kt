package com.rarible.protocol.generator.lang.kotlin

import com.rarible.protocol.generator.Generator
import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeMapperFactory
import com.rarible.protocol.generator.lang.AbstractGeneratorFactory
import com.rarible.protocol.generator.type.ProvidedTypeReader

class KotlinGeneratorFactory(
    private val packageName: String
) : AbstractGeneratorFactory() {

    private val qualifierGenerator: QualifierGenerator = QualifierGenerator { "$packageName.${it}Dto" }

    override fun getGeneratorWithDefaultTypes(
        primitiveTypeFileReader: ProvidedTypeReader,
        providedTypeFileReader: ProvidedTypeReader,
        typeMapperFactory: TypeMapperFactory
    ): Generator {
        return KotlinGenerator(
            getLang(),
            packageName,
            primitiveTypeFileReader,
            providedTypeFileReader,
            typeMapperFactory,
            getQualifierGenerator()
        )
    }

    override fun getLang(): String {
        return "kotlin"
    }

    override fun getQualifierGenerator(): QualifierGenerator {
        return qualifierGenerator
    }
}
