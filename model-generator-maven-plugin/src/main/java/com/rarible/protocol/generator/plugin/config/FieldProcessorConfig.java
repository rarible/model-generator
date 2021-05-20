package com.rarible.protocol.generator.plugin.config;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.Map;

public class FieldProcessorConfig {

    @Parameter
    private String apiPathReplacement;

    @Parameter
    private String apiPathReplacementRegex;

    @Parameter
    private String template;

    @Parameter
    private Map<String, String> placeholders;

    public String getApiPathReplacement() {
        return apiPathReplacement;
    }

    public void setApiPathReplacement(String apiPathReplacement) {
        this.apiPathReplacement = apiPathReplacement;
    }

    public String getApiPathReplacementRegex() {
        return apiPathReplacementRegex;
    }

    public void setApiPathReplacementRegex(String apiPathReplacementRegex) {
        this.apiPathReplacementRegex = apiPathReplacementRegex;
    }

    public Map<String, String> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(Map<String, String> placeholders) {
        this.placeholders = placeholders;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
