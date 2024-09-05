package com.rarible.protocol.merger.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.rarible.protocol.merger.SchemaMerger
import com.reprezen.jsonoverlay.JsonLoader
import org.apache.commons.lang3.StringUtils
import java.io.File


class OpenapiSchemaMerger(
) : SchemaMerger {

    override fun mergeSchemas(originalText: String, dependenciesTexts: List<String>, dest: File) {
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
        }

        YAMLMapper().writeValue(dest, main)
    }

    private fun mergeOrSet(main: JsonNode, dep: JsonNode, fieldName: String) {
        val mainField = main.get(fieldName)
        val extField = dep.get(fieldName)
        if (extField == null || !extField.isObject) {
            return
        }
        if (mainField != null && mainField.isObject) {
            val main = mainField as ObjectNode
            val ext = extField as ObjectNode

            val iter = ext.fields()
            while (iter.hasNext()) {
                val (name, depField) = iter.next()
                main.set<JsonNode>(name, depField)
            }
        } else {
            (main as ObjectNode).set<JsonNode>(fieldName, extField)
        }
    }
}
