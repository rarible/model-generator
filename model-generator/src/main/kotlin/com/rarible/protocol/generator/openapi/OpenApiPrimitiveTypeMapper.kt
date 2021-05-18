package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.component.SimpleComponent
import com.rarible.protocol.generator.exception.SchemaValidationException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class OpenApiPrimitiveTypeMapper(
    types: Map<String, String>
) {

    private val log = LoggerFactory.getLogger(OpenApiPrimitiveTypeMapper::class.java)
    private val definitions: Map<String, SimpleComponent>

    init {
        log.debug("Reading primitive definitions:")
        definitions = types.mapValuesTo(java.util.HashMap()) {
            log.debug("\t${it.key} -> ${it.value}")
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
