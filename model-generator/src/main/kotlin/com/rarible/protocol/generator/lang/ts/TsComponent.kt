package com.rarible.protocol.generator.lang.ts

import com.rarible.protocol.generator.component.ComponentField
import com.rarible.protocol.generator.component.GeneratedComponent
import com.rarible.protocol.generator.lang.LangComponent
import com.rarible.protocol.generator.lang.LangEnum

class TsComponent(
    parent: LangComponent?,
    definition: GeneratedComponent
) : LangComponent(
    parent,
    definition
) {

    override fun createFieldEnum(field: ComponentField): LangEnum {
        return LangEnum(
            getSimpleClassName(getQualifier()) + "_" + field.name.capitalize(),
            field.enumValues
        )
    }

    override fun getSimpleClassName(qualifier: String): String {
        return qualifier
    }

    override fun fromComponent(parent: LangComponent, definition: GeneratedComponent): LangComponent {
        return TsComponent(parent, definition)
    }
}