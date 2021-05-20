package com.rarible.protocol.generator.plugin.mapper;

import com.rarible.protocol.generator.TypeMapperFactory;
import com.rarible.protocol.merger.SchemaFieldNameProcessor;
import com.rarible.protocol.merger.SchemaMerger;
import com.rarible.protocol.merger.SchemaProcessor;

public interface TypeMapperSettings {

    String getDefaultSchemaPath();

    TypeMapperFactory getTypeMapperFactory();

    SchemaMerger getSchemaMerger();

    SchemaProcessor getSchemaProcessor(SchemaFieldNameProcessor fieldNameProcessor);

}
