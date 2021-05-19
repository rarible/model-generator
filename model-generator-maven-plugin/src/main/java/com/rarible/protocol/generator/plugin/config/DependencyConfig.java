package com.rarible.protocol.generator.plugin.config;

import org.apache.maven.plugins.annotations.Parameter;

public class DependencyConfig {

    @Parameter
    private String name;
    @Parameter
    private String packageName;

    private String jarFile;

    @Parameter
    private String schemaFile;

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

    public String getPackageName() {
        return packageName;
    }
}
