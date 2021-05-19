package com.rarible.protocol.merger.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.rarible.protocol.merger.SchemaFieldNameProcessor
import com.rarible.protocol.merger.SchemaMerger
import com.reprezen.jsonoverlay.JsonLoader
import java.io.File
import java.net.URL


class OpenapiSchemaMerger(
    private val fieldNameProcessor: SchemaFieldNameProcessor
) : SchemaMerger {

    constructor() : this(SchemaFieldNameProcessor.NO_OP)

    override fun mergeSchemas(origin: File, dest: File, schemaTexts: List<String>) {
        val loader = JsonLoader()
        var originUrl: URL
        if (origin.exists()) {
            originUrl = origin.toURI().toURL()
        } else {
            originUrl = javaClass.getResource("/openapi.yaml")
        }
        val root = loader.load(originUrl) as ObjectNode

        for (text in schemaTexts) {
            val json = loader.loadString(null, text) as ObjectNode
            mergeOrSet(root, json, "components")
            mergeOrSet(root, json, "paths")
        }

        val mergedPaths = root.get("paths")
        if (mergedPaths != null && mergedPaths.isObject) {
            val paths = mergedPaths as ObjectNode
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

        YAMLMapper().writeValue(dest, root)
    }

    private fun mergeOrSet(root: ObjectNode, ext: ObjectNode, fieldName: String) {
        val mainField = root.get(fieldName)
        val extField = ext.get(fieldName)
        if (mainField != null && !mainField.isNull) {
            mergeObjectFields(mainField, extField)
        } else if (extField != null) {
            root.set<JsonNode>(fieldName, extField)
        }

    }

    private fun mergeObjectFields(main: JsonNode, ext: JsonNode?) {
        if (main == null || ext == null || !main.isObject || !ext.isObject) {
            return
        }
        val mainObject = main as ObjectNode
        val extObject = ext as ObjectNode

        val iter = main.fields()
        while (iter.hasNext()) {
            val (name, mainField) = iter.next()
            val extField = ext[name]
            if (extField != null && !extField.isNull) {
                when {
                    mainField.isArray -> {
                        mergeArrayFields(mainField, extField);
                    }
                    mainField.isObject -> {
                        mergeObjectFields(mainField, extField)
                    }
                    else -> {
                        mainObject.set<JsonNode>(name, extField)
                    }
                }
            }
            extObject.remove(name)
        }

        val extIterator = ext.fields()
        while (extIterator.hasNext()) {
            val (key, value) = extIterator.next()
            mainObject.set<JsonNode>(key, value)
        }
    }

    private fun mergeArrayFields(main: JsonNode, ext: JsonNode?) {
        if (main == null || ext == null || !main.isArray || !ext.isArray) {
            return
        }
        (main as ArrayNode).addAll(ext as ArrayNode)
    }

}
