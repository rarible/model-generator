package com.rarible.protocol.generator.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.rarible.protocol.generator.QualifierGenerator
import com.rarible.protocol.generator.TypeMapper
import com.rarible.protocol.generator.TypeMapperFactory
import com.reprezen.jsonoverlay.JsonLoader
import java.io.File

class OpenApiTypeMapperFactory : TypeMapperFactory {

    override fun getTypeDefinitionMapper(
        qualifierGenerator: QualifierGenerator,
        primitiveTypeMapping: Map<String, String>,
        providedTypeMapping: Map<String, String>
    ): TypeMapper {

        return OpenApiTypeMapper(
            qualifierGenerator,
            OpenApiPrimitiveTypeMapper(primitiveTypeMapping),
            OpenApiProvidedTypeMapper(providedTypeMapping)
        )
    }

    override fun mergeSchemas(origin: File, dest: File, schemaTexts: List<String>) {
        val loader = JsonLoader()
        val root = loader.load(origin.toURI().toURL()) as ObjectNode

        for (text in schemaTexts) {
            val json = loader.loadString(null, text) as ObjectNode
            mergeOrSet(root, json, "components")
            mergeOrSet(root, json, "paths")
        }

        YAMLMapper().writeValue(dest, root)
    }

    private fun mergeOrSet(root: ObjectNode, ext: ObjectNode, fieldName: String) {
        val mainField = root.get(fieldName)
        val extField = ext.get(fieldName)
        if (mainField != null) {
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
