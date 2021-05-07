package com.rarible.protocol.generator.plugin;

import java.util.Properties;

public class GeneratorConfig {

    private String lang;
    private Properties properties;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
