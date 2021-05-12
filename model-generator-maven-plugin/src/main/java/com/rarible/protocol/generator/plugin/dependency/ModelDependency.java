package com.rarible.protocol.generator.plugin.dependency;

import java.util.Map;

public class ModelDependency {

    private String jarFile;
    private String text;
    private Map<String, String> primitiveTypes;
    private Map<String, String> providedTypes;
    private Map<String, String> generatedProvidedTypes;

    public String getJarFile() {
        return jarFile;
    }

    public void setJarFile(String jarFile) {
        this.jarFile = jarFile;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, String> getPrimitiveTypes() {
        return primitiveTypes;
    }

    public void setPrimitiveTypes(Map<String, String> primitiveTypes) {
        this.primitiveTypes = primitiveTypes;
    }

    public Map<String, String> getProvidedTypes() {
        return providedTypes;
    }

    public void setProvidedTypes(Map<String, String> providedTypes) {
        this.providedTypes = providedTypes;
    }

    public Map<String, String> getGeneratedProvidedTypes() {
        return generatedProvidedTypes;
    }

    public void setGeneratedProvidedTypes(Map<String, String> generatedProvidedTypes) {
        this.generatedProvidedTypes = generatedProvidedTypes;
    }
}
