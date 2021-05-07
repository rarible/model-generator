package com.rarible.protocol.generator.template

import freemarker.cache.TemplateLoader
import java.io.*
import java.nio.file.Path

class ResourceTemplateLoader(
    private val templatesFolder: Path
) : TemplateLoader {

    override fun findTemplateSource(name: String): InputStream {
        val templateFile = templatesFolder.resolve("$name.ftl")
        return FileInputStream(templateFile.toFile())
    }

    override fun getLastModified(templateSource: Any): Long {
        return 0
    }

    @Throws(IOException::class)
    override fun getReader(templateSource: Any, encoding: String): Reader {
        return InputStreamReader(templateSource as InputStream, encoding)
    }

    @Throws(IOException::class)
    override fun closeTemplateSource(templateSource: Any) {
        (templateSource as InputStream).close()
    }

}
