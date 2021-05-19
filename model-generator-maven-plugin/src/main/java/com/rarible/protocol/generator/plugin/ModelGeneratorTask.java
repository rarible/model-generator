package com.rarible.protocol.generator.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rarible.protocol.generator.Generator;
import com.rarible.protocol.generator.GeneratorFactory;
import com.rarible.protocol.generator.plugin.config.GeneratorConfig;
import com.rarible.protocol.generator.plugin.dependency.ModelDependencyManager;
import com.rarible.protocol.generator.plugin.dependency.SchemaDependencyManager;
import com.rarible.protocol.generator.plugin.lang.GeneratorRegistry;
import com.rarible.protocol.generator.type.ProvidedTypeCascadeReader;
import com.rarible.protocol.generator.type.ProvidedTypeConstantReader;
import com.rarible.protocol.generator.type.ProvidedTypeFileReader;
import com.rarible.protocol.generator.type.ProvidedTypeReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class ModelGeneratorTask {

    private Log log;
    private MavenProject project;
    private ObjectMapper objectMapper = new ObjectMapper();

    private String lang;
    private String modelOutputDirectory;

    private GeneratorConfig generatorConfig;
    private GeneratorFactory generatorFactory;

    private ModelDependencyManager modelDependencyManager;
    private final SchemaDependencyManager schemaDependencyManager;

    private final File mergedSchemaFile;

    public ModelGeneratorTask(
            Log log,
            MavenProject project,
            SchemaDependencyManager schemaDependencyManager,
            File mergedSchemaFile,
            GeneratorConfig generatorConfig
    ) throws MojoExecutionException, IOException {

        this.log = log;
        this.project = project;
        this.schemaDependencyManager = schemaDependencyManager;
        this.mergedSchemaFile = mergedSchemaFile;
        this.generatorConfig = generatorConfig;

        lang = generatorConfig.getLang();

        modelOutputDirectory = generatorConfig.getModelOutputDirectory();
        checkOutputDir();

        modelDependencyManager = new ModelDependencyManager(
                log,
                lang,
                schemaDependencyManager
        );

        generatorFactory = GeneratorRegistry.getGeneratorFactory(lang, generatorConfig.getPackageName());
    }

    private void checkOutputDir() {
        if (StringUtils.isBlank(modelOutputDirectory)) {
            modelOutputDirectory = Folders.getDefaultGeneratedSourcesFolder(project.getBasedir(), lang).toString();
        }
        log.info("Directory for generated classes: " + modelOutputDirectory);
    }

    public void execute() throws MojoExecutionException, IOException {
        ProvidedTypeReader primitiveTypeReader = getCascadePrimitiveTypeReader(
                modelDependencyManager,
                generatorConfig.getPrimitiveTypesFile(),
                lang
        );

        ProvidedTypeReader providedTypeReader = getCascadeProvidedTypeReader(
                modelDependencyManager,
                generatorConfig.getProvidedTypesFile(),
                lang
        );

        mergeProvidedTypes(primitiveTypeReader, providedTypeReader);

        Generator generator = generatorFactory.getGenerator(
                primitiveTypeReader,
                providedTypeReader,
                schemaDependencyManager.getTypeMapperSettings().getTypeMapperFactory());

        generator.generate(
                Paths.get(mergedSchemaFile.toURI()),
                Paths.get(modelOutputDirectory)
        );
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
}
