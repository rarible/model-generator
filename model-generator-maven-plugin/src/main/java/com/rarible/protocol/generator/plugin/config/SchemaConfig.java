package com.rarible.protocol.generator.plugin.config;

import org.apache.maven.plugins.annotations.Parameter;

public class SchemaConfig {

    private String type;
    private String inputFile;
    private String outputFile;
    @Parameter(defaultValue = "false")
    private boolean mergeTags;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInputFile() {
        return inputFile;
    }

    public boolean isRemote() {
        return inputFile != null && inputFile.startsWith("http");
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public Boolean getMergeTags() {
        return mergeTags;
    }

    public void setMergeTags(Boolean mergeTags) {
        this.mergeTags = mergeTags;
    }
}
