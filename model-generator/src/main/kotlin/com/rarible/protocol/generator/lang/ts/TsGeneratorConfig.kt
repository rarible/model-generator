package com.rarible.protocol.generator.lang.ts

data class TsGeneratorConfig(
    /**
     * Suffix for enum type names. E.g., "Enum" will yield "BlockchainEnum" type for "blockchain" model type
     * */
    val enumSuffix: String
)