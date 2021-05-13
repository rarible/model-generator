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

        String modelOutputDirectory = task.getModelOutputDirectory();
        String schemaOutputDirectory = task.getModelOutputDirectory();
        if (StringUtils.isBlank(modelOutputDirectory)) {
            modelOutputDirectory = new File(project.getBasedir(), "target/generated-sources").toString();
        }
        if (StringUtils.isBlank(schemaOutputDirectory)) {
            schemaOutputDirectory = new File(project.getBasedir(), "target/classes").toString();
        }

        getLog().info("Directory for generated classes: " + modelOutputDirectory);
        getLog().info("Directory for merged schema: " + modelOutputDirectory);

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
        File mergedSchemaFile = new File(schemaOutputDirectory, schemaFile.getName());
        mergedSchemaFile.getParentFile().mkdirs();

        getLog().info("Merging all schemas into file: " + mergedSchemaFile);

        typeMapperFactory.mergeSchemas(schemaFile, mergedSchemaFile, dependencyManager.getSchemaTexts());

        List<ProvidedTypeReader> primitiveTypeReaders = getCascadePrimitiveTypeReaders(
                dependencyManager,
                task.getPrimitiveTypesFile(),
                lang
        );

        List<ProvidedTypeReader> providedTypeReaders = getCascadeProvidedTypeReaders(
                dependencyManager,
                task.getProvidedTypesFile(),
                lang
        );

        Generator generator = generatorFactory.getGenerator(
                new ProvidedTypeCascadeReader(primitiveTypeReaders),
                new ProvidedTypeCascadeReader(providedTypeReaders),
                typeMapperFactory
        );

        generator.generate(
                Paths.get(mergedSchemaFile.toURI()),
                Paths.get(modelOutputDirectory)
        );
    }

    private List<ProvidedTypeReader> getCascadePrimitiveTypeReaders(ModelDependencyManager dependencyManager,
                                                                    String primitiveTypesFile,
                                                                    String lang) throws MojoExecutionException {

        File defaultPrimitiveTypesFile = new File(
                project.getBasedir(),
                "src/main/resources/model-generator/" + lang + "/primitives.json"
        );

        List<ProvidedTypeReader> primitiveTypeReaders = dependencyManager.getPrimitiveTypeReaders();
        getLog().debug("Combined primitive types from dependencies: "
                + new ProvidedTypeCascadeReader(primitiveTypeReaders).getMapping());

        primitiveTypeReaders.add(getTypeReader(primitiveTypesFile, defaultPrimitiveTypesFile));
        getLog().debug("Full list of primitive types: "
                + new ProvidedTypeCascadeReader(primitiveTypeReaders).getMapping());

        return primitiveTypeReaders;
    }

    private List<ProvidedTypeReader> getCascadeProvidedTypeReaders(ModelDependencyManager dependencyManager,
                                                                   String providedTypesFile,
                                                                   String lang) throws MojoExecutionException {

        File defaultProvidedTypesFile = new File(
                project.getBasedir(),
                "src/main/resources/model-generator/" + lang + "/provided.json"
        );

        List<ProvidedTypeReader> providedTypeReaders = dependencyManager.getProvidedTypeReaders();
        getLog().debug("Combined provided types from dependencies: "
                + new ProvidedTypeCascadeReader(providedTypeReaders).getMapping());

        providedTypeReaders.add(getTypeReader(providedTypesFile, defaultProvidedTypesFile));
        getLog().debug("Full list of provided types: "
                + new ProvidedTypeCascadeReader(providedTypeReaders).getMapping());

        return providedTypeReaders;
    }

    private ProvidedTypeReader getTypeReader(String filePath, File defaultFile) throws MojoExecutionException {
        if (filePath == null) {
            if (defaultFile.exists()) {
                getLog().debug("Reading default file with types: " + defaultFile);
                return new ProvidedTypeFileReader(defaultFile);
            } else {
                getLog().debug("Default file with types not found, skipping: " + defaultFile);
                return new ProvidedTypeConstantReader(new HashMap<>());
            }
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new MojoExecutionException("File not found: " + filePath);
        }
        return new ProvidedTypeFileReader(file);
    }

    private void applyFullJarNames(List<DependencyConfig> dependencyConfigs) throws MojoExecutionException {
        Set<Artifact> artifacts = getDependencies();
        Map<String, Artifact> mapped = artifacts.stream().collect(Collectors.toMap(
                Artifact::getArtifactId,
                (a) -> a,
                // Duplicated jar names possible for some external dependencies we are not interested in
                (a, b) -> a
        ));
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
