package com.rarible.protocol.merger

class PlaceholderFieldNameProcessor(
    private val template: String,
    private val placeholders: Map<String, String>
) : SchemaFieldNameProcessor {

    override fun process(originalPath: String): String {
        var result = template
        for ((key, value) in placeholders) {
            result = result.replace(key, value)
        }
        result = result.replace("{originalPath}", originalPath)
        return result
    }
}
