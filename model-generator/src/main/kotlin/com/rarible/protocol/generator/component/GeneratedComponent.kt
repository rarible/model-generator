package com.rarible.protocol.generator.component

data class GeneratedComponent(
    override val name: String,
    override val qualifier: String,
    val fields: Map<String, ComponentField>,
    val discriminator: Discriminator?
) : AbstractComponent()
