package com.rarible.protocol.generator.plugin.lang;

import com.rarible.protocol.generator.GeneratorFactory;
import com.rarible.protocol.generator.lang.kotlin.KotlinGeneratorFactory;
import com.rarible.protocol.generator.lang.ts.TsGeneratorFactory;
import com.rarible.protocol.generator.plugin.config.GeneratorConfig;
import org.apache.maven.plugin.MojoExecutionException;

public class GeneratorRegistry {

    public static final String KOTLIN = "kotlin";
    public static final String TS = "ts";

    public static GeneratorFactory getGeneratorFactory(GeneratorConfig generatorConfig) throws MojoExecutionException {
        String lang = generatorConfig.getLang();
        if (KOTLIN.equals(lang)) {
            return new KotlinGeneratorFactory(generatorConfig.getPackageName());
        }
        if (TS.equals(lang)) {
            return new TsGeneratorFactory(generatorConfig.getPackageName(), generatorConfig.getTsEnumSuffix());
        }
        throw new MojoExecutionException("Model generation for lang '" + lang + "' is not supported");
    }

}
