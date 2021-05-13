package com.rarible.protocol.generator.lang.kotlin

import com.rarible.protocol.generator.AbstractGeneratorFactory
import com.rarible.protocol.generator.Generator
import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeMapperFactory
import com.rarible.protocol.generator.type.ProvidedTypeReader

class KotlinGeneratorFactory(
    private val packageName: String,
    private val withInheritance: Boolean
) : AbstractGeneratorFactory() {

    private val qualifierGenerator: QualifierGenerator = QualifierGenerator { "$packageName.$it" }

    override fun getGeneratorWithDefaultTypes(
        primitiveTypesFileReader: ProvidedTypeReader,
        providedTypesFileReader: ProvidedTypeReader,
        typeMapperFactory: TypeMapperFactory
    ): Generator {
        return KotlinGenerator(
            getLang(),
            primitiveTypesFileReader,
            providedTypesFileReader,
            typeMapperFactory,
            getQualifierGenerator(),
            withInheritance
        )
    }

    override fun getLang(): String {
        return "kotlin"
    }

    override fun getQualifierGenerator(): QualifierGenerator {
        return qualifierGenerator
    }
}
