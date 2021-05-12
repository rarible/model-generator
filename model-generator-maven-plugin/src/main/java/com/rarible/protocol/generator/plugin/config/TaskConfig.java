package com.rarible.protocol.generator.plugin.config;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.ArrayList;
import java.util.List;

public class TaskConfig {

    @Parameter
    private SchemaConfig schema;

    @Parameter
    private GeneratorConfig generator;

    @Parameter
    private String primitiveTypesFile;

    @Parameter
    private String providedTypesFile;

    @Parameter
    private String modelOutputDirectory;

    @Parameter
    private String schemaOutputDirectory;

    private List<DependencyConfig> dependencies = new ArrayList<>();

    public List<DependencyConfig> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<DependencyConfig> dependencies) {
        this.dependencies = dependencies;
    }

    public SchemaConfig getSchema() {
        return schema;
    }

    public void setSchema(SchemaConfig schema) {
        this.schema = schema;
    }

    public GeneratorConfig getGenerator() {
        return generator;
    }

    public void setGenerator(GeneratorConfig generator) {
        this.generator = generator;
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

    public String getModelOutputDirectory() {
        return modelOutputDirectory;
    }

    public void setModelOutputDirectory(String modelOutputDirectory) {
        this.modelOutputDirectory = modelOutputDirectory;
    }

    public String getSchemaOutputDirectory() {
        return schemaOutputDirectory;
    }

    public void setSchemaOutputDirectory(String schemaOutputDirectory) {
        this.schemaOutputDirectory = schemaOutputDirectory;
    }
}
