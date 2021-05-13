package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.component.SimpleComponent
import com.rarible.protocol.generator.exception.SchemaValidationException

class OpenApiProvidedTypeMapper(
    types: Map<String, String>
) {

    private val definitions: Map<String, SimpleComponent>

    init {
        definitions = types.mapValuesTo(java.util.HashMap()) {
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
