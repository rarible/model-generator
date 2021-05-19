package com.rarible.protocol.generator.openapi

import com.rarible.protocol.generator.exception.IllegalOperationException
import com.reprezen.kaizen.oasparser.model3.Schema
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

internal class OpenApiComponentTest {

    @Test
    fun `test field not found`() {
        val schema = mock(Schema::class.java)
        doReturn("Item").`when`(schema).name
        doReturn(ArrayList<String>()).`when`(schema).requiredFields

        val component = OpenApiComponent(schema)
        assertThrows(IllegalOperationException::class.java) { component.getField("notexist") }
    }

    @Test
    fun `test illegal oneof calls`() {
        val schema = mock(Schema::class.java)
        doReturn("Item").`when`(schema).name
        doReturn(ArrayList<String>()).`when`(schema).requiredFields
        doReturn(ArrayList<String>()).`when`(schema).oneOfSchemas

        val component = OpenApiComponent(schema)
        assertThrows(IllegalOperationException::class.java) { component.getDiscriminatorField() }
        assertThrows(IllegalOperationException::class.java) { component.getOneOf() }
    }

}
