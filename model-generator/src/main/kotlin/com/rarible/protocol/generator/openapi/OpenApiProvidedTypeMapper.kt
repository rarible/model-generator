package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.component.SimpleComponent
import com.rarible.protocol.generator.exception.SchemaValidationException
import org.slf4j.LoggerFactory

class OpenApiProvidedTypeMapper(
    types: Map<String, String>
) {

    private val log = LoggerFactory.getLogger(OpenApiPrimitiveTypeMapper::class.java)
    private val definitions: Map<String, SimpleComponent>

    init {
        log.debug("Reading provided types definitions:")
        definitions = types.mapValuesTo(java.util.HashMap()) {
            log.debug("\t${it.key} -> ${it.value}")
            SimpleComponent(it.key, it.value)
        }
    }

    fun has(name: String): Boolean {
        return definitions.containsKey(name)
    }

    fun getDefinition(componentName: String): SimpleComponent {
        return definitions[componentName]
            ?: throw SchemaValidationException("Not found mapping for provided type $componentName")
    }
}
