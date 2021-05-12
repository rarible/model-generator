package com.rarible.protocol.generator

abstract class AbstractGenerator(
    val lang: String,
    val typeMapperFactory: TypeMapperFactory
) : Generator {

    val templateFolder: String = "/model-generator/$lang"

}


