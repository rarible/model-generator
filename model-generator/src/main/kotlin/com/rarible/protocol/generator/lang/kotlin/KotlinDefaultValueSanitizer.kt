package com.rarible.protocol.generator.lang.kotlin

import org.apache.commons.lang3.StringUtils

object KotlinDefaultValueSanitizer {

    private fun init(type: String, sanitizer: (value: Any) -> String): KotlinSimpleDefaultValueSanitizer {
        return KotlinSimpleDefaultValueSanitizer(type, sanitizer)
    }

    private val sanitizers = listOf(
        init("Boolean") { it.toString().toLowerCase() },
        init("Byte") { it.toString() },
        init("Int") { it.toString() },
        init("Long") { it.toString() },
        init("Float") { it.toString() },
        init("Double") { it.toString() },
        init("String") { "\"$it\"" },
        init("BigDecimal") { "BigDecimal(\"$it\")" },
        init("BigInteger") { "BigInteger(\"$it\")" }
    ).associateBy { it.type }

    fun sanitize(type: String, value: Any): String {
        val sanitizer = sanitizers[type] ?: return "$type.$value"
        return sanitizer.sanitize(value)
    }

    fun sanitize(type: String, values: List<Any>): String {
        val sanitized = values.map { sanitize(type, it) }
        return "listOf(${StringUtils.join(sanitized, ", ")})"
    }

    private class KotlinSimpleDefaultValueSanitizer(
        val type: String,
        val sanitize: (value: Any) -> String
    )

}