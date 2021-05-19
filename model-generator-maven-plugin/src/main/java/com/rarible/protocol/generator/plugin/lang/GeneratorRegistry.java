package com.rarible.protocol.generator.plugin.lang;

import com.rarible.protocol.generator.GeneratorFactory;
import com.rarible.protocol.generator.lang.kotlin.KotlinGeneratorFactory;
import org.apache.maven.plugin.MojoExecutionException;

public class GeneratorRegistry {

    public static final String KOTLIN = "kotlin";

    public static GeneratorFactory getGeneratorFactory(String lang, String packageName) throws MojoExecutionException {
        if (KOTLIN.equals(lang)) {
            return new KotlinGeneratorFactory(packageName);
        }
        throw new MojoExecutionException("Model generation for lang '" + lang + "' is not supported");
    }

}
