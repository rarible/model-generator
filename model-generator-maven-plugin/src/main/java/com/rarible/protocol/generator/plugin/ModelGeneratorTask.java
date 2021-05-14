package com.rarible.protocol.generator.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ModelGeneratorTask {

    private Log log;
    private MavenProject project;
    private TaskConfig task;
    private ObjectMapper objectMapper = new ObjectMapper();

    private String lang;

    private GeneratorConfig generatorConfig;
    private Properties properties;
    private SchemaConfig schema;

    private String modelOutputDirectory;
    private String schemaOutputDirectory;

    private GeneratorFactory generatorFactory;
    private TypeMapperSettings typeMapperSettings;
    private TypeMapperFactory typeMapperFactory;

    private ModelDependencyManager depManager;

    File inputSchemaFile;
    File mergedSchemaFile;

    public ModelGeneratorTask(Log log, MavenProject project, TaskConfig task) throws MojoExecutionException, IOException {
        this.log = log;
        this.project = project;
        this.task = task;

        lang = task.getGenerator().getLang();
        generatorConfig = task.getGenerator();
        properties = generatorConfig.getProperties();
        schema = task.getSchema();

        modelOutputDirectory = task.getModelOutputDirectory();
        schemaOutputDirectory = task.getModelOutputDirectory();
        checkOutputDirs();

        inputSchemaFile = new File(schema.getFileName());
        mergedSchemaFile = new File(schemaOutputDirectory, inputSchemaFile.getName());
        mergedSchemaFile.getParentFile().mkdirs();

        typeMapperSettings = TypeMapperRegistry.getTypeMapperSettings(schema.getType());
        typeMapperFactory = typeMapperSettings.getTypeMapperFactory();
        generatorFactory = GeneratorRegistry.getGeneratorFactory(lang, properties);
    }

    private void checkOutputDirs() {
        if (StringUtils.isBlank(modelOutputDirectory)) {
            modelOutputDirectory = new File(project.getBasedir(), "target/generated-sources").toString();
        }
        if (StringUtils.isBlank(schemaOutputDirectory)) {
            schemaOutputDirectory = new File(project.getBasedir(), "target/classes").toString();
        }

        log.info("Directory for generated classes: " + modelOutputDirectory);
        log.info("Directory for merged schema: " + modelOutputDirectory);
    }

    public void execute(TaskConfig task) throws MojoExecutionException, IOException {
        applyFullJarNames(task.getDependencies());

        depManager = new ModelDependencyManager(
                log,
                lang,
                task.getDependencies(),
                typeMapperSettings
        );

        ProvidedTypeReader primitiveTypeReader = getCascadePrimitiveTypeReader(depManager, task.getPrimitiveTypesFile(), lang);
        ProvidedTypeReader providedTypeReader = getCascadeProvidedTypeReader(depManager, task.getProvidedTypesFile(), lang);

        mergeSchemas();
        mergeProvidedTypes(primitiveTypeReader, providedTypeReader);

        Generator generator = generatorFactory.getGenerator(primitiveTypeReader, providedTypeReader, typeMapperFactory);

        generator.generate(
                Paths.get(mergedSchemaFile.toURI()),
                Paths.get(modelOutputDirectory)
        );
    }

    private void mergeSchemas() {
        List<String> depSchemaTexts = depManager.getSchemaTexts();
        log.info("Merging " + depSchemaTexts.size() + " schemas into file: " + mergedSchemaFile);
        for (String text : depSchemaTexts) {
            log.debug("-----------------------------------");
            log.debug(text);
        }
        typeMapperFactory.mergeSchemas(inputSchemaFile, mergedSchemaFile, depSchemaTexts);
    }

    private void mergeProvidedTypes(ProvidedTypeReader primitiveTypeReader, ProvidedTypeReader providedTypeReader) throws IOException {
        File mergedPrimitiveTypesFile = Folders.getMergedPrimitiveTypesFilePath(mergedSchemaFile.getParentFile(), lang);
        mergedPrimitiveTypesFile.getParentFile().mkdirs();
        log.info("Merging all primitive types into file: " + mergedPrimitiveTypesFile);
        objectMapper.writeValue(mergedPrimitiveTypesFile, primitiveTypeReader.getMapping());

        File mergedProvidedTypesFile = Folders.getMergedProvidedTypesFilePath(mergedSchemaFile.getParentFile(), lang);
        mergedProvidedTypesFile.getParentFile().mkdirs();
        log.info("Merging all provided types into file: " + mergedProvidedTypesFile);
        objectMapper.writeValue(mergedProvidedTypesFile, providedTypeReader.getMapping());
    }

    private ProvidedTypeReader getCascadePrimitiveTypeReader(ModelDependencyManager dependencyManager,
                                                             String primitiveTypesFile,
                                                             String lang) throws MojoExecutionException {

        File defaultPrimitiveTypesFile = Folders.getDefaultPrimitiveTypesFilePath(project.getBasedir(), lang);

        List<ProvidedTypeReader> primitiveTypeReaders = dependencyManager.getPrimitiveTypeReaders();
        log.debug("Combined primitive types from dependencies: "
                + new ProvidedTypeCascadeReader(primitiveTypeReaders).getMapping());

        primitiveTypeReaders.add(getTypeReader(primitiveTypesFile, defaultPrimitiveTypesFile));
        log.debug("Full list of primitive types: "
                + new ProvidedTypeCascadeReader(primitiveTypeReaders).getMapping());

        return new ProvidedTypeCascadeReader(primitiveTypeReaders);
    }

    private ProvidedTypeReader getCascadeProvidedTypeReader(ModelDependencyManager dependencyManager,
                                                            String providedTypesFile,
                                                            String lang) throws MojoExecutionException {

        File defaultProvidedTypesFile = Folders.getDefaultProvidedTypesFilePath(project.getBasedir(), lang);

        List<ProvidedTypeReader> providedTypeReaders = dependencyManager.getProvidedTypeReaders();
        log.debug("Combined provided types from dependencies: "
                + new ProvidedTypeCascadeReader(providedTypeReaders).getMapping());

        providedTypeReaders.add(getTypeReader(providedTypesFile, defaultProvidedTypesFile));
        log.debug("Full list of provided types: "
                + new ProvidedTypeCascadeReader(providedTypeReaders).getMapping());

        return new ProvidedTypeCascadeReader(providedTypeReaders);
    }

    private ProvidedTypeReader getTypeReader(String filePath, File defaultFile) throws MojoExecutionException {
        if (filePath == null) {
            if (defaultFile.exists()) {
                log.debug("Reading default file with types: " + defaultFile);
                return new ProvidedTypeFileReader(defaultFile);
            } else {
                log.debug("Default file with types not found, skipping: " + defaultFile);
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
