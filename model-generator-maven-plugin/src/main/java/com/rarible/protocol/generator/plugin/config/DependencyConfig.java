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

    private FieldProcessorConfig pathProcessor;

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

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public FieldProcessorConfig getPathProcessor() {
        return pathProcessor;
    }

    public void setPathProcessor(FieldProcessorConfig pathProcessor) {
        this.pathProcessor = pathProcessor;
    }
}
