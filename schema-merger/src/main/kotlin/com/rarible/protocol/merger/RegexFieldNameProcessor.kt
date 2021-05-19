package com.rarible.protocol.merger

import java.util.regex.Pattern

class RegexFieldNameProcessor(
    regex: String,
    private val replacement: String
) : SchemaFieldNameProcessor {

    private val pattern = Pattern.compile(regex)

    override fun process(originalPath: String): String {
        return pattern.matcher(originalPath).replaceAll(replacement)
    }
}
