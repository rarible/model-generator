package com.rarible.protocol.generator.plugin.dependency;

import com.rarible.protocol.generator.plugin.config.DependencyConfig;
import com.rarible.protocol.generator.plugin.mapper.TypeMapperSettings;
import com.rarible.protocol.merger.SchemaFieldNameProcessor;
import com.rarible.protocol.merger.SchemaProcessor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SchemaDependencyProvider {

    private Log log;
    private String jarFile;
    private String schemaFile;
    private String defaultSchemaFile;
    private SchemaProcessor pathProcessor;

    public SchemaDependencyProvider(Log log,
                                    DependencyConfig dependency,
                                    TypeMapperSettings typeMapperSettings
    ) {
        this.log = log;
        this.jarFile = dependency.getJarFile();
        this.schemaFile = dependency.getSchemaFile();
        this.defaultSchemaFile = typeMapperSettings.getDefaultSchemaPath();

        FieldNameProcessorFactory fieldNameProcessorFactory = new FieldNameProcessorFactory();
        SchemaFieldNameProcessor processor = fieldNameProcessorFactory.create(dependency.getPathProcessor());
        this.pathProcessor = typeMapperSettings.getSchemaProcessor(processor);
    }

    public SchemaDependency getDependency() throws IOException {
        log.debug("Opening jar file: " + jarFile);
        try (DependencyReader reader = DependencyReaderFactory.getReader(jarFile)) {
            SchemaDependency result = new SchemaDependency();
            result.setJarFile(jarFile);
            result.setProcessedText(pathProcessor.process(readSchemaText(reader)));
            return result;
        }
    }

    private String readSchemaText(DependencyReader reader) throws IOException {
        InputStream stream;
        if (StringUtils.isNotBlank(schemaFile)) {
            log.debug("Trying to read schema file " + reader.getPath() + " -> " + schemaFile);
            stream = reader.getInputStream(schemaFile);
            if (stream == null) {
                throw new IOException("Schema file '" + schemaFile + "'" +
                        " not found in jar '" + reader.getPath() + "'");
            }
        } else {
            log.debug("Custom schema path not specified, trying to read default schema file: "
                    + reader.getPath() + " -> " + defaultSchemaFile);

            stream = reader.getInputStream(defaultSchemaFile);
            if (stream == null) {
                throw new IOException("Default schema file '" + defaultSchemaFile + "' not found" +
                        " in jar '" + reader.getPath() + "' and no custom path for schema specified");
            }
        }
        try (InputStream in = stream) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
    }

}
