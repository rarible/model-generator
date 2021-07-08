package com.rarible.protocol.generator.lang

import java.nio.file.Path

class InMemoryClassWriter(val output: MutableMap<String, String>) : ClassWriter {
    override fun write(langClass: LangClass, text: String, outputFolder: Path) {
        output[langClass.name] = text
    }
}