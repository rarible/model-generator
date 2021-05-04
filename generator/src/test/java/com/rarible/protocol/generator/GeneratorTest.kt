package com.rarible.protocol.generator

import com.rarible.protocol.generator.type.ExternalTypeDefinition
import com.rarible.protocol.generator.type.InternalTypeDefinition
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.nio.file.Paths
import javax.mail.Address

internal class GeneratorTest {

    @Test
    fun test() {

        val internalTypeDefinitions = listOf(
            InternalTypeDefinition.of("array", List::class.java),
            InternalTypeDefinition.of("string", String::class.java),
            InternalTypeDefinition.of("integer", Integer::class.java)
        )

        val externalTypeDefinitions = listOf(
            ExternalTypeDefinition.of("Address", Address::class.java),
            ExternalTypeDefinition.of("BigInt", BigInteger::class.java)
        )

        val mapper = OpenApiTypeDefinitionMapper(
            "com.rarible.test",
            internalTypeDefinitions,
            externalTypeDefinitions
        )

        val reader = OpenApiReader(Paths.get("src/test/resources/schema.yml"))

        val generatedDefinitions = mapper.getGeneratedComponents(reader.openApi)

        generatedDefinitions.forEach {
            print(it.toString())
        }
    }
}
