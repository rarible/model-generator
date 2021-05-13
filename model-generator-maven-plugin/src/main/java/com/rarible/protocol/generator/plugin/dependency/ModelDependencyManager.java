package com.rarible.protocol.generator.plugin.dependency;

import com.rarible.protocol.generator.GeneratorFactory;
import com.rarible.protocol.generator.plugin.config.DependencyConfig;
import com.rarible.protocol.generator.plugin.lang.GeneratorRegistry;
import com.rarible.protocol.generator.plugin.mapper.TypeMapperSettings;
import com.rarible.protocol.generator.type.ProvidedTypeConstantReader;
import com.rarible.protocol.generator.type.ProvidedTypeReader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModelDependencyManager {

    private final Log log;

    private final String lang;
    private final List<DependencyConfig> dependencies;
    private final TypeMapperSettings typeMapperSettings;

    private List<ModelDependency> modelDependencies = new ArrayList<>();

    private List<ProvidedTypeReader> primitiveTypeReaders = new ArrayList<>();
    private List<ProvidedTypeReader> providedTypeReaders = new ArrayList<>();

    public ModelDependencyManager(Log log,
                                  String lang,
                                  List<DependencyConfig> dependencies,
                                  TypeMapperSettings typeMapperSettings
    ) throws MojoExecutionException, IOException {
        this.log = log;
        this.lang = lang;
        this.dependencies = dependencies;
        this.typeMapperSettings = typeMapperSettings;
        readDependencies();
    }

    private void readDependencies() throws MojoExecutionException, IOException {
        List<ModelDependencyProvider> providers = getDependencyProviders();
        for (ModelDependencyProvider provider : providers) {
            ModelDependency dependency = provider.getDependency();
            modelDependencies.add(dependency);
            primitiveTypeReaders.add(new ProvidedTypeConstantReader(dependency.getPrimitiveTypes()));
            providedTypeReaders.add(new ProvidedTypeConstantReader(dependency.getProvidedTypes()));
            providedTypeReaders.add(new ProvidedTypeConstantReader(dependency.getGeneratedProvidedTypes()));
            log.info("Added " + dependency.getGeneratedProvidedTypes().size() + " generated models " +
                    "from external jar: " + dependency.getJarFile());
        }
    }

    private List<ModelDependencyProvider> getDependencyProviders() throws MojoExecutionException {

        List<ModelDependencyProvider> dependencyProviders = new ArrayList<>();
        for (DependencyConfig dependencyConfig : dependencies) {
            log.info("Reading schema from jar: " + dependencyConfig.getName());
            GeneratorFactory dependencyGeneratorFactory = GeneratorRegistry.getGeneratorFactory(
                    lang,
                    dependencyConfig.getProperties()
            );

            ModelDependencyProvider dependencyProvider = new ModelDependencyProvider(
                    log,
                    dependencyConfig,
                    dependencyGeneratorFactory,
                    typeMapperSettings
            );
            dependencyProviders.add(dependencyProvider);
        }
        return dependencyProviders;
    }

    public List<ProvidedTypeReader> getPrimitiveTypeReaders() {
        return primitiveTypeReaders;
    }

    public List<ProvidedTypeReader> getProvidedTypeReaders() {
        return providedTypeReaders;
    }

    public List<String> getSchemaTexts() {
        return modelDependencies.stream().map(ModelDependency::getText).collect(Collectors.toList());
    }
}
