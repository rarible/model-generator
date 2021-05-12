package com.rarible.protocol.generator.plugin.config;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.Properties;

public class DependencyConfig {

    @Parameter
    private String name;

    private String jarFile;

    @Parameter
    private String schemaFile;

    @Parameter
    private String primitiveTypesFile;

    @Parameter
    private String providedTypesFile;

    @Parameter
    private Properties properties = new Properties();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJarFile() {
        return jarFile;
    }

    public void setJarFile(String jarFile) {
        this.jarFile = jarFile;
    }

    public String getSchemaFile() {
        return schemaFile;
    }

    public void setSchemaFile(String schemaFile) {
        this.schemaFile = schemaFile;
    }

    public String getPrimitiveTypesFile() {
        return primitiveTypesFile;
    }

    public void setPrimitiveTypesFile(String primitiveTypesFile) {
        this.primitiveTypesFile = primitiveTypesFile;
    }

    public String getProvidedTypesFile() {
        return providedTypesFile;
    }

    public void setProvidedTypesFile(String providedTypesFile) {
        this.providedTypesFile = providedTypesFile;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
