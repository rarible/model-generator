package com.rarible.protocol.generator

import com.rarible.protocol.generator.component.GeneratedComponent
import java.nio.file.Path

interface TypeMapper {

    fun readGeneratedComponents(path: Path): List<GeneratedComponent>

}
