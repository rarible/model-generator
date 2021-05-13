package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.component.SimpleComponent
import com.rarible.protocol.generator.exception.SchemaValidationException
import org.apache.commons.lang3.StringUtils

class OpenApiPrimitiveTypeMapper(
    types: Map<String, String>
) {

    private val definitions: Map<String, SimpleComponent>

    init {
        definitions = types.mapValuesTo(java.util.HashMap()) {
            SimpleComponent(it.key, it.value)
        }
    }

    fun getDefinition(type: String): SimpleComponent {
        return getDefinition(type, null)
    }

    fun getDefinition(type: String, format: String?): SimpleComponent {
        val key = if (StringUtils.isBlank(format)) type else "$type:$format"
        return definitions[key]
            ?: throw SchemaValidationException("Not found mapping for primitive type $key")
    }

}
