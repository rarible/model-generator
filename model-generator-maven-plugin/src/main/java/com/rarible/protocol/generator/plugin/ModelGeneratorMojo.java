package com.rarible.protocol.generator.plugin;

import com.rarible.protocol.generator.TypeMapperFactory;
import com.rarible.protocol.generator.lang.kotlin.KotlinGenerator;
import com.rarible.protocol.generator.openapi.OpenApiTypeMapperFactory;
import com.rarible.protocol.generator.type.ProvidedTypeConstantReader;
import com.rarible.protocol.generator.type.ProvidedTypeFileReader;
import com.rarible.protocol.generator.type.ProvidedTypeReader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ModelGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter
    private List<TaskConfig> tasks;

    @Override
    public void execute() throws MojoExecutionException {

        for (TaskConfig task : tasks) {
            String lang = task.getGenerator().getLang();

            getLog().info("Starting to generate classes for lang: " + lang
                    + "\n\t--- Schema type: " + task.getSchema().getType()
                    + "\n\t--- Schema file: " + task.getSchema().getFileName()
                    + "\n\t--- Primitive types mapping file: " + task.getPrimitiveTypesFile()
                    + "\n\t--- Provided types mapping file: " + task.getProvidedTypesFile()
                    + "\n\t--- Output directory: " + task.getOutputDirectory()
                    + "\n\t--- Properties: " + task.getGenerator().getProperties()
            );

            if (lang.equals("kotlin")) {
                generateKotlin(task);
                continue;
            }
            throw new MojoExecutionException("Model generation for lang '" + lang + "' is not supported");
        }
    }

    private void generateKotlin(TaskConfig task) throws MojoExecutionException {
        SchemaConfig schemaConfig = task.getSchema();
        GeneratorConfig generatorConfig = task.getGenerator();
        Properties properties = generatorConfig.getProperties();

        ProvidedTypeReader primitiveTypesReader = task.getPrimitiveTypesFile() == null
                ? new ProvidedTypeConstantReader(new HashMap<>())
                : new ProvidedTypeFileReader(new File(task.getPrimitiveTypesFile()));

        ProvidedTypeReader providedTypesReader = task.getProvidedTypesFile() == null
                ? new ProvidedTypeConstantReader(new HashMap<>())
                : new ProvidedTypeFileReader(new File(task.getProvidedTypesFile()));

        KotlinGenerator generator = new KotlinGenerator(
                primitiveTypesReader,
                providedTypesReader,
                getTypeMapperFactory(schemaConfig.getType()),
                properties.getProperty("packageName"),
                Boolean.valueOf(properties.getProperty("withInheritance", "false"))
        );

        generator.generate(
                Paths.get(schemaConfig.getFileName()),
                Paths.get(task.getOutputDirectory())
        );
    }

    private TypeMapperFactory getTypeMapperFactory(String type) throws MojoExecutionException {
        if ("openapi".equals(type)) {
            return new OpenApiTypeMapperFactory();
        }
        throw new MojoExecutionException("Schema type '" + type + "' is not supported");
    }
}
