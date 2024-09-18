package com.rarible.protocol.generator.plugin.config;

import org.apache.maven.plugins.annotations.Parameter;

public class GeneratorConfig {

    @Parameter
    private String lang;
    @Parameter
    private String packageName;
    @Parameter
    private String primitiveTypesFile;
    @Parameter
    private String providedTypesFile;
    @Parameter
    private String modelOutputDirectory;
    @Parameter
    private String tsEnumSuffix = "";

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public String getTsEnumSuffix() {
        return tsEnumSuffix;
    }

    public void setTsEnumSuffix(String tsEnumSuffix) {
        this.tsEnumSuffix = tsEnumSuffix;
    }
}
