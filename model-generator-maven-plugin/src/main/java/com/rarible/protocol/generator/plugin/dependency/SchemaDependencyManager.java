package com.rarible.protocol.generator.plugin.dependency;

import com.rarible.protocol.generator.plugin.config.DependencyConfig;
import com.rarible.protocol.generator.plugin.mapper.TypeMapperSettings;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SchemaDependencyManager {

    private final Log log;

    private final List<DependencyConfig> dependencies;
    private final TypeMapperSettings typeMapperSettings;

    private final Map<String, SchemaDependency> schemaDependencies = new LinkedHashMap<>();

    public SchemaDependencyManager(Log log,
                                   List<DependencyConfig> dependencies,
                                   TypeMapperSettings typeMapperSettings
    ) throws IOException {
        this.log = log;
        this.dependencies = dependencies;
        this.typeMapperSettings = typeMapperSettings;
        readDependencies();
    }

    public List<DependencyConfig> getDependencies() {
        return dependencies;
    }

    public TypeMapperSettings getTypeMapperSettings() {
        return typeMapperSettings;
    }

    private void readDependencies() throws IOException {
        List<SchemaDependencyProvider> providers = getDependencyProviders();
        for (SchemaDependencyProvider provider : providers) {
            SchemaDependency dependency = provider.getDependency();
            schemaDependencies.put(dependency.getJarFile(), dependency);
        }
    }

    private List<SchemaDependencyProvider> getDependencyProviders() {

        List<SchemaDependencyProvider> dependencyProviders = new ArrayList<>();
        for (DependencyConfig dependencyConfig : dependencies) {
            log.info("Reading schema from jar: " + dependencyConfig.getName());

            SchemaDependencyProvider dependencyProvider = new SchemaDependencyProvider(
                    log,
                    dependencyConfig,
                    typeMapperSettings);

            dependencyProviders.add(dependencyProvider);
        }
        return dependencyProviders;
    }

    public String getSchemaText(String jarName) {
        return schemaDependencies.get(jarName).getText();
    }

    public List<String> getSchemaTexts() {
        return schemaDependencies.values().stream().map(SchemaDependency::getText).collect(Collectors.toList());
    }

}
