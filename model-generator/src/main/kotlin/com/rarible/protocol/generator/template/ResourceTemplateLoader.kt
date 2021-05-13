package com.rarible.protocol.generator.template

import freemarker.cache.TemplateLoader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

class ResourceTemplateLoader(
    private val templatesFolder: String
) : TemplateLoader {

    override fun findTemplateSource(name: String): InputStream {
        return javaClass.getResourceAsStream("$templatesFolder/$name.ftl")
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
