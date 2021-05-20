package com.rarible.protocol.merger.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.rarible.protocol.merger.SchemaFieldNameProcessor
import com.rarible.protocol.merger.SchemaProcessor
import com.reprezen.jsonoverlay.JsonLoader

class OpenapiSchemaProcessor(
    private val fieldNameProcessor: SchemaFieldNameProcessor
) : SchemaProcessor {

    override fun process(text: String): String {
        val loader = JsonLoader()
        val root = loader.loadString(null, text)
        val pathNode = root.get("paths")

        if (pathNode != null && pathNode.isObject) {
            val paths = pathNode as ObjectNode
            val iter = paths.fields()
            val mapping = LinkedHashMap<String, String>()
            while (iter.hasNext()) {
                val field = iter.next()
                mapping.put(field.key, fieldNameProcessor.process(field.key))
            }
            for ((oldName, newName) in mapping) {
                val field = paths.remove(oldName) as ObjectNode
                paths.set<JsonNode>(newName, field)
            }
        }
        return YAMLMapper().writeValueAsString(root)
    }

}
