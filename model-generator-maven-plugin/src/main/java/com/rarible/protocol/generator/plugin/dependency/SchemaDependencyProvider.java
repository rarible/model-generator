package com.rarible.protocol.generator.plugin.dependency;

import com.rarible.protocol.generator.plugin.config.DependencyConfig;
import com.rarible.protocol.generator.plugin.mapper.TypeMapperSettings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class SchemaDependencyProvider {

    private Log log;
    private String jarFile;
    private String schemaFile;
    private String defaultSchemaFile;

    public SchemaDependencyProvider(Log log,
                                    DependencyConfig dependency,
                                    TypeMapperSettings typeMapperSettings
    ) {
        this.log = log;
        this.jarFile = dependency.getJarFile();
        this.schemaFile = dependency.getSchemaFile();
        this.defaultSchemaFile = typeMapperSettings.getDefaultSchemaPath();
    }

    public SchemaDependency getDependency() throws IOException {
        log.debug("Opening jar file: " + jarFile);
        try (JarFile jar = new JarFile(jarFile)) {
            SchemaDependency result = new SchemaDependency();
            result.setJarFile(jarFile);
            result.setText(readSchemaText(jar));
            return result;
        }
    }

    private String readSchemaText(JarFile jar) throws IOException {
        ZipEntry zipEntry;
        if (StringUtils.isNotBlank(schemaFile)) {
            log.debug("Trying to read schema file " + jar.getName() + " -> " + schemaFile);
            zipEntry = jar.getEntry(schemaFile);
            if (zipEntry == null) {
                throw new IOException("Schema file '" + schemaFile + "'" +
                        " not found in jar '" + jar.getName() + "'");
            }
        } else {
            log.debug("Custom schema path not specified, trying to read default schema file: "
                    + jar.getName() + " -> " + defaultSchemaFile);

            zipEntry = jar.getEntry(defaultSchemaFile);
            if (zipEntry == null) {
                throw new IOException("Default schema file '" + defaultSchemaFile + "' not found" +
                        " in jar '" + jar.getName() + "' and no custom path for schema specified");
            }
        }
        try (InputStream stream = jar.getInputStream(zipEntry)) {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        }
    }

}
