package com.rarible.protocol.generator.component

import com.reprezen.kaizen.oasparser.model3.Schema

interface GeneratedFile {
    val name: String //Part
}

data class SingleModelFile(
    override val name: String,
    val schema: GeneratedComponentDefinition
) : GeneratedFile

data class MultipleModelFile(
    override val name: String,
    val children: List<GeneratedComponentDefinition>
) : GeneratedFile