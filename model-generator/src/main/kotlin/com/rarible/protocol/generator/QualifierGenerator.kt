package com.rarible.protocol.generator

fun interface QualifierGenerator {

    fun getQualifier(name: String, isEnum: Boolean): String

}
