package com.rarible.protocol.generator.component

class SimpleComponentDefinition(
    override var name: String?,
    override var packageName: String?,
    var openApiType: String
) : ComponentDefinition() {

}


