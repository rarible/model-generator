package com.rarible.protocol.generator

import com.reprezen.kaizen.oasparser.OpenApi3Parser
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import java.nio.file.Path

class OpenApiReader(path: Path) {

    val openApi: OpenApi3

    init {
        val parser = OpenApi3Parser()
        openApi = parser.parse(path.toFile())
    }
}
