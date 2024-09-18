package com.rarible.protocol.merger.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.rarible.protocol.merger.SchemaMerger
import com.reprezen.jsonoverlay.JsonLoader
import org.apache.commons.lang3.StringUtils
import java.io.File


class OpenapiSchemaMerger(
) : SchemaMerger {

    override fun mergeSchemas(
        originalText: String,
        dependenciesTexts: MutableList<String>,
        dest: File,
        mergeTags: Boolean
    ) {
        val loader = JsonLoader()
        val main: ObjectNode = if (StringUtils.isBlank(originalText)) {
            loader.load(javaClass.getResource("/openapi.yaml")) as ObjectNode
        } else {
            loader.loadString(null, originalText) as ObjectNode
        }

        for (text in dependenciesTexts) {
            val dep = loader.loadString(null, text) as ObjectNode
            val mainComponents = main.get("components")
            val depComponents = dep.get("components")

            if (depComponents != null && depComponents.isObject) {
                if (mainComponents == null || mainComponents.isNull) {
                    main.set<JsonNode>("components", depComponents)
                } else {
                    mergeOrSet(mainComponents, depComponents, "schemas")
                    mergeOrSet(mainComponents, depComponents, "responses")
                }
            }
            mergeOrSet(main, dep, "paths")
            mergeOrSet(main, dep, "info")
            if (mergeTags) {
                mergeTags(main, dep)
            }
        }

        YAMLMapper().writeValue(dest, main)
    }

    private fun mergeTags(main: JsonNode, dep: JsonNode) {
        main as ObjectNode

        val mainField = main.get("tags")
        val extField = dep.get("tags")
        if (extField == null || !extField.isArray) {
            return
        }
        val result = main.arrayNode()
        val mainIterable = mainField ?: emptyList()
        val extMap = extField.associateBy { it.get("name").textValue() }.toMutableMap()
        mainIterable.forEach {
            result.add(it)
            extMap.remove(it.get("name").textValue())
        }
        result.addAll(extMap.values)
        main.set<ArrayNode>("tags", result)
    }

    private fun mergeOrSet(main: JsonNode, dep: JsonNode, fieldName: String) {
        val mainField = main.get(fieldName)
        val extField = dep.get(fieldName)
        if (extField == null || !extField.isObject) {
            return
        }
        if (mainField != null && mainField.isObject) {
            val mainValue = mainField as ObjectNode
            val extValue = extField as ObjectNode

            val iter = extValue.fields()
            while (iter.hasNext()) {
                val (name, depField) = iter.next()
                mainValue.set<JsonNode>(name, depField)
            }
        } else {
            (main as ObjectNode).set<JsonNode>(fieldName, extField)
        }
    }
}
