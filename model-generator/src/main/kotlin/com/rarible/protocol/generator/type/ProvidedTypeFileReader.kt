package com.rarible.protocol.generator.type

import java.io.File
import java.io.FileInputStream

class ProvidedTypeFileReader(
    filePath: File
) : ProvidedTypeStreamReader(
    FileInputStream(filePath)
)
