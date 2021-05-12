package com.rarible.protocol.generator.plugin.mapper;

import com.rarible.protocol.generator.openapi.OpenApiTypeMapperFactory;
import org.apache.maven.plugin.MojoExecutionException;

public class TypeMapperRegistry {

    public static final String OPENAPI = "openapi";

    public static TypeMapperSettings getTypeMapperSettings(String mapperType) throws MojoExecutionException {
        if (OPENAPI.equals(mapperType)) {
            return getOpenApi();
        }
        throw new MojoExecutionException("Schema type '" + mapperType + "' is not supported");
    }

    private static TypeMapperSettings getOpenApi() {
        TypeMapperSettings result = new TypeMapperSettings();
        result.setTypeMapperFactory(new OpenApiTypeMapperFactory());
        result.setDefaultSchemaPath("openapi.yaml");
        return result;
    }
}
