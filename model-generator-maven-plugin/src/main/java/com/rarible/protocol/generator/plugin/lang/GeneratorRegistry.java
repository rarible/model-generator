package com.rarible.protocol.generator.plugin.lang;

import com.rarible.protocol.generator.GeneratorFactory;
import com.rarible.protocol.generator.lang.kotlin.KotlinGeneratorFactory;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.Properties;

public class GeneratorRegistry {

    public static final String KOTLIN = "kotlin";

    public static GeneratorFactory getGeneratorFactory(String lang, Properties properties) throws MojoExecutionException {
        if (KOTLIN.equals(lang)) {
            return getKotlin(properties);
        }
        throw new MojoExecutionException("Model generation for lang '" + lang + "' is not supported");
    }

    private static final String PROPERTY_PACKAGE_NAME = "packageName";
    private static final String PROPERTY_WITH_INHERITANCE = "withInheritance";

    private static GeneratorFactory getKotlin(Properties properties) throws MojoExecutionException {
        String packageName = properties.getProperty(PROPERTY_PACKAGE_NAME, null);
        if (packageName == null) {
            throw new MojoExecutionException("Property '" + PROPERTY_PACKAGE_NAME + "' not specified for Kotlin generator");
        }

        return new KotlinGeneratorFactory(packageName);
    }

}
