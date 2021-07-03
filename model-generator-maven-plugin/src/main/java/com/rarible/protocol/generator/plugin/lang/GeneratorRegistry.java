package com.rarible.protocol.generator.plugin.lang;

import com.rarible.protocol.generator.GeneratorFactory;
import com.rarible.protocol.generator.lang.kotlin.KotlinGeneratorFactory;
import com.rarible.protocol.generator.lang.ts.TsGeneratorFactory;
import org.apache.maven.plugin.MojoExecutionException;

public class GeneratorRegistry {

    public static final String KOTLIN = "kotlin";
    public static final String TS = "ts";

    public static GeneratorFactory getGeneratorFactory(String lang, String packageName) throws MojoExecutionException {
        if (KOTLIN.equals(lang)) {
            return new KotlinGeneratorFactory(packageName);
        }
        if (TS.equals(lang)) {
            return new TsGeneratorFactory(packageName);
        }
        throw new MojoExecutionException("Model generation for lang '" + lang + "' is not supported");
    }

}
