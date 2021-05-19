package com.rarible.protocol.generator.plugin.mapper;

import com.rarible.protocol.generator.TypeMapperFactory;
import com.rarible.protocol.merger.SchemaMerger;

public class TypeMapperSettings {

    private String defaultSchemaPath;
    private TypeMapperFactory typeMapperFactory;
    private SchemaMerger schemaMerger;

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

    public SchemaMerger getSchemaMerger() {
        return schemaMerger;
    }

    public void setSchemaMerger(SchemaMerger schemaMerger) {
        this.schemaMerger = schemaMerger;
    }
}
