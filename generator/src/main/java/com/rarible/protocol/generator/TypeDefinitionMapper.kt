package com.rarible.protocol.generator

import com.rarible.protocol.generator.component.GeneratedComponentDefinition
import java.nio.file.Path

interface TypeDefinitionMapper {

    fun readGeneratedComponents(path: Path): List<GeneratedComponentDefinition>

}
