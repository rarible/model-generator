package com.rarible.protocol.generator.lang.ts

import com.rarible.protocol.generator.component.ComponentField
import com.rarible.protocol.generator.component.GeneratedComponent
import com.rarible.protocol.generator.lang.LangComponent
import com.rarible.protocol.generator.lang.LangEnum
import com.rarible.protocol.generator.lang.LangField

class TsComponent(
    parent: LangComponent?,
    definition: GeneratedComponent,
    private val tsGeneratorConfig: TsGeneratorConfig,
) : LangComponent(
    parent,
    definition
) {

    override fun createFieldEnum(field: ComponentField): LangEnum {
        return LangEnum(
            getSimpleClassName(getQualifier()) + field.nameCapitalized + tsGeneratorConfig.enumSuffix,
            field.enumValues
        )
    }

    override fun getSimpleClassName(qualifier: String): String {
        val fileAndName = qualifier.split(":")
        return if (fileAndName.size == 1) fileAndName[0] else fileAndName[1]
    }

    override fun fromComponent(parent: LangComponent, definition: GeneratedComponent): LangComponent {
        return TsComponent(parent, definition, tsGeneratorConfig)
    }

    override fun sanitizeDefaultValue(field: LangField): LangField {
        return field
    }
}