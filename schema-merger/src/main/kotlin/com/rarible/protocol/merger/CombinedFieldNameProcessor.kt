package com.rarible.protocol.merger

class CombinedFieldNameProcessor(
    private val processors: List<SchemaFieldNameProcessor>
) : SchemaFieldNameProcessor {

    override fun process(originalPath: String): String {
        var result = originalPath
        for (processor in processors) {
            result = processor.process(result)
        }
        return result
    }
}
