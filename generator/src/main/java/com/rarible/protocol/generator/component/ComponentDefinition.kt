package com.rarible.protocol.generator.component

abstract class ComponentDefinition {

    abstract var name: String?
    abstract var packageName: String?

    fun toClassName(): String? {
        return "$packageName.$name"
    }
}
