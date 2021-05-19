package com.rarible.protocol.merger

import java.io.File

interface SchemaMerger {

    fun mergeSchemas(origin: File, dest: File, schemaTexts: List<String>)
}
