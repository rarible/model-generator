package com.rarible.protocol.generator.plugin;

import com.rarible.protocol.generator.Generator;
import com.rarible.protocol.generator.GeneratorFactory;
import com.rarible.protocol.generator.TypeMapperFactory;
import com.rarible.protocol.generator.plugin.config.DependencyConfig;
import com.rarible.protocol.generator.plugin.config.GeneratorConfig;
import com.rarible.protocol.generator.plugin.config.SchemaConfig;
import com.rarible.protocol.generator.plugin.config.TaskConfig;
import com.rarible.protocol.generator.plugin.dependency.ModelDependencyManager;
import com.rarible.protocol.generator.plugin.lang.GeneratorRegistry;
import com.rarible.protocol.generator.plugin.mapper.TypeMapperRegistry;
import com.rarible.protocol.generator.plugin.mapper.TypeMapperSettings;
import com.rarible.protocol.generator.type.ProvidedTypeCascadeReader;
import com.rarible.protocol.generator.type.ProvidedTypeConstantReader;
import com.rarible.protocol.generator.type.ProvidedTypeFileReader;
import com.rarible.protocol.generator.type.ProvidedTypeReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ModelGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter
    private List<TaskConfig> tasks;

    @Override
    public void execute() throws MojoExecutionException {
        for (TaskConfig task : tasks) {
            try {
                executeTask(task);
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    private void executeTask(TaskConfig task) throws MojoExecutionException, IOException {
        String lang = task.getGenerator().getLang();
        GeneratorConfig generatorConfig = task.getGenerator();
        Properties properties = generatorConfig.getProperties();
        SchemaConfig schema = task.getSchema();

        String outputDirectory = task.getOutputDirectory();
        if (StringUtils.isBlank(outputDirectory)) {
            outputDirectory = new File(project.getBasedir(), "target/generated-sources").toString();
        }

        getLog().info("Directory for generated classes: " + outputDirectory);

        TypeMapperSettings typeMapperSettings = TypeMapperRegistry.getTypeMapperSettings(schema.getType());
        TypeMapperFactory typeMapperFactory = typeMapperSettings.getTypeMapperFactory();
        GeneratorFactory generatorFactory = GeneratorRegistry.getGeneratorFactory(lang, properties);

        applyFullJarNames(task.getDependencies());
        ModelDependencyManager dependencyManager = new ModelDependencyManager(
                getLog(),
                lang,
                task.getDependencies(),
                typeMapperSettings
        );

        File schemaFile = new File(schema.getFileName());
        File mergedSchemaFile = new File(task.getOutputDirectory(), schemaFile.getName());
        mergedSchemaFile.getParentFile().mkdirs();

        getLog().info("Merging all schemas into file: " + mergedSchemaFile);
        typeMapperFactory.mergeSchemas(
                schemaFile,
                mergedSchemaFile,
                dependencyManager.getSchemaTexts()
        );

        List<ProvidedTypeReader> primitiveTypeReaders = dependencyManager.getPrimitiveTypeReaders();
        List<ProvidedTypeReader> providedTypeReaders = dependencyManager.getProvidedTypeReaders();

        primitiveTypeReaders.add(getTypeReader(task.getPrimitiveTypesFile()));
        providedTypeReaders.add(getTypeReader(task.getProvidedTypesFile()));

        Generator generator = generatorFactory.getGenerator(
                new ProvidedTypeCascadeReader(primitiveTypeReaders),
                new ProvidedTypeCascadeReader(providedTypeReaders),
                typeMapperFactory
        );

        generator.generate(
                Paths.get(mergedSchemaFile.toURI()),
                Paths.get(task.getOutputDirectory())
        );
    }

    private ProvidedTypeReader getTypeReader(String filePath) {
        return filePath == null
                ? new ProvidedTypeConstantReader(new HashMap<>())
                : new ProvidedTypeFileReader(new File(filePath));
    }

    private void applyFullJarNames(List<DependencyConfig> dependencyConfigs) throws MojoExecutionException {
        Set<Artifact> artifacts = getDependencies();
        Map<String, Artifact> mapped = artifacts.stream().collect(Collectors.toMap(
                Artifact::getArtifactId,
                (a) -> a)
        );
        for (DependencyConfig dep : dependencyConfigs) {
            Artifact a = mapped.get(dep.getName());
            dep.setJarFile(a.getFile().toString());
        }
    }

    private Set<Artifact> getDependencies() throws MojoExecutionException {
        Field field = null;
        try {
            field = project.getClass().getDeclaredField("resolvedArtifacts");
            field.setAccessible(true);
            Set<Artifact> result = (Set<Artifact>) field.get(project);
            return result;
        } catch (ReflectiveOperationException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            field.setAccessible(false);
        }
    }
}
