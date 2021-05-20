package com.rarible.protocol.merger

import java.io.File

interface SchemaMerger {

    fun mergeSchemas(originalText: String, dependenciesTexts: List<String>, dest: File)
}
