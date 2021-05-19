package com.rarible.protocol.generator.plugin.mapper;

import com.rarible.protocol.generator.openapi.OpenApiTypeMapperFactory;
import com.rarible.protocol.merger.SchemaFieldNameProcessor;
import com.rarible.protocol.merger.openapi.OpenapiSchemaMerger;
import org.apache.maven.plugin.MojoExecutionException;

public class TypeMapperRegistry {

    public static final String OPENAPI = "openapi";

    public static TypeMapperSettings getTypeMapperSettings(
            String mapperType,
            SchemaFieldNameProcessor fieldNameProcessor
    ) throws MojoExecutionException {

        if (OPENAPI.equals(mapperType)) {
            return getOpenApi(fieldNameProcessor);
        }
        throw new MojoExecutionException("Schema type '" + mapperType + "' is not supported");
    }

    private static TypeMapperSettings getOpenApi(SchemaFieldNameProcessor fieldNameProcessor) {
        TypeMapperSettings result = new TypeMapperSettings();
        result.setTypeMapperFactory(new OpenApiTypeMapperFactory());
        result.setDefaultSchemaPath("openapi.yaml");
        result.setSchemaMerger(new OpenapiSchemaMerger(fieldNameProcessor));
        return result;
    }
}
