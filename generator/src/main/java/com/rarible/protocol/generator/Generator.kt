package com.rarible.protocol.generator

import java.nio.file.Path

interface Generator {

    fun generate(apiFilePath: Path, outputFolder: Path)

}
