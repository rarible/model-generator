package com.rarible.protocol.generator.plugin.mapper;

import com.rarible.protocol.generator.openapi.OpenApiTypeMapperFactory;
import com.rarible.protocol.merger.SchemaFieldNameProcessor;
import com.rarible.protocol.merger.SchemaProcessor;
import com.rarible.protocol.merger.openapi.OpenapiSchemaMerger;
import com.rarible.protocol.merger.openapi.OpenapiSchemaProcessor;
import org.apache.maven.plugin.MojoExecutionException;

public class TypeMapperRegistry {

    public static final String OPENAPI = "openapi";

    public static TypeMapperSettings getTypeMapperSettings(
            String mapperType
    ) throws MojoExecutionException {

        if (OPENAPI.equals(mapperType)) {
            return getOpenApi();
        }
        throw new MojoExecutionException("Schema type '" + mapperType + "' is not supported");
    }

    private static TypeMapperSettings getOpenApi() {
        OpenapiTypeMapperSettings result = new OpenapiTypeMapperSettings();
        result.setTypeMapperFactory(new OpenApiTypeMapperFactory());
        result.setDefaultSchemaPath("openapi.yaml");
        result.setSchemaMerger(new OpenapiSchemaMerger());
        return result;
    }

    private static class OpenapiTypeMapperSettings extends AbstractTypeMapperSettings {

        @Override
        public SchemaProcessor getSchemaProcessor(SchemaFieldNameProcessor fieldNameProcessor) {
            return new OpenapiSchemaProcessor(fieldNameProcessor);
        }
    }
}
