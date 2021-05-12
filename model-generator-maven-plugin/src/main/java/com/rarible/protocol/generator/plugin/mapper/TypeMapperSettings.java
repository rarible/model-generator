package com.rarible.protocol.generator.plugin.mapper;

import com.rarible.protocol.generator.TypeMapperFactory;

public class TypeMapperSettings {

    private String defaultSchemaPath;
    private TypeMapperFactory typeMapperFactory;

    public String getDefaultSchemaPath() {
        return defaultSchemaPath;
    }

    public void setDefaultSchemaPath(String defaultSchemaPath) {
        this.defaultSchemaPath = defaultSchemaPath;
    }

    public TypeMapperFactory getTypeMapperFactory() {
        return typeMapperFactory;
    }

    public void setTypeMapperFactory(TypeMapperFactory typeMapperFactory) {
        this.typeMapperFactory = typeMapperFactory;
    }
}
