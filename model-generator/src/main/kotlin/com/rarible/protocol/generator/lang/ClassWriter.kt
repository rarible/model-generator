package com.rarible.protocol.generator.lang

import java.nio.file.Path

fun interface ClassWriter {

    fun write(langClass: LangClass, text: String, outputFolder: Path)

}