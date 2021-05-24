package com.rarible.protocol.generator.plugin.dependency;

import com.rarible.protocol.generator.GeneratorFactory;
import com.rarible.protocol.generator.QualifierGenerator;
import com.rarible.protocol.generator.TypeMapper;
import com.rarible.protocol.generator.TypeMapperFactory;
import com.rarible.protocol.generator.component.GeneratedComponent;
import com.rarible.protocol.generator.plugin.Folders;
import com.rarible.protocol.generator.plugin.config.DependencyConfig;
import com.rarible.protocol.generator.plugin.mapper.TypeMapperSettings;
import com.rarible.protocol.generator.type.ProvidedTypeStreamReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelDependencyProvider {

    private Log log;

    private final String jarFile;
    private final String defaultSchemaFile;

    private final QualifierGenerator qualifierGenerator;
    private final TypeMapperFactory typeMapperFactory;
    private final GeneratorFactory generatorFactory;

    private final SchemaDependencyManager schemaDependencyManager;

    public ModelDependencyProvider(Log log,
                                   DependencyConfig dependency,
                                   GeneratorFactory generatorFactory,
                                   TypeMapperSettings typeMapperSettings,
                                   SchemaDependencyManager schemaDependencyManager
    ) {
        this.log = log;
        this.jarFile = dependency.getJarFile();
        this.defaultSchemaFile = typeMapperSettings.getDefaultSchemaPath();
        this.qualifierGenerator = generatorFactory.getQualifierGenerator();
        this.generatorFactory = generatorFactory;
        this.typeMapperFactory = typeMapperSettings.getTypeMapperFactory();
        this.schemaDependencyManager = schemaDependencyManager;
    }

    public ModelDependency getDependency() throws IOException {
        log.debug("Reading dependency: " + jarFile);

        try (DependencyReader reader = DependencyReaderFactory.getReader(jarFile)) {
            ModelDependency result = new ModelDependency();
            result.setJarFile(jarFile);

            Map<String, String> defaultPrimitiveTypes = generatorFactory.getDefaultPrimitiveTypesReader().getMapping();
            Map<String, String> defaultProvidedTypes = generatorFactory.getDefaultProvidedTypesReader().getMapping();

            String lang = generatorFactory.getLang();
            String primitiveTypesFile = Folders.getMergedPrimitiveTypesFileRelativePath(lang);
            String providedTypesFile = Folders.getMergedProvidedTypesFileRelativePath(lang);

            defaultPrimitiveTypes.putAll(readTypes(reader, primitiveTypesFile));
            defaultProvidedTypes.putAll(readTypes(reader, providedTypesFile));
            log.debug("Primitive types (default included): " + defaultPrimitiveTypes);
            log.debug("Provided types (default included): " + defaultProvidedTypes);

            result.setPrimitiveTypes(defaultPrimitiveTypes);
            result.setProvidedTypes(defaultProvidedTypes);

            TypeMapper mapper = typeMapperFactory.getTypeDefinitionMapper(
                    qualifierGenerator,
                    result.getPrimitiveTypes(),
                    result.getProvidedTypes()
            );

            List<GeneratedComponent> providedComponents = readComponents(mapper,
                    schemaDependencyManager.getSchemaText(jarFile),
                    defaultSchemaFile
            );

            Map<String, String> generatedProvidedTypes = providedComponents.stream().collect(Collectors.toMap(
                    GeneratedComponent::getName,
                    GeneratedComponent::getQualifier
            ));

            result.setGeneratedProvidedTypes(generatedProvidedTypes);
            return result;
        }
    }

    private List<GeneratedComponent> readComponents(TypeMapper mapper, String text, String fileName) throws IOException {
        String extension = StringUtils.substringAfterLast(fileName, ".");
        File file = File.createTempFile(fileName, "." + extension);
        log.debug("Writing components from schema to the temp file: " + file);
        try {
            FileUtils.write(file, text, StandardCharsets.UTF_8);
            return mapper.readGeneratedComponents(Paths.get(file.toURI()));
        } finally {
            log.debug("Removing temp file " + file);
            file.delete();
        }
    }

    private Map<String, String> readTypes(DependencyReader reader, String defaultFilePath) throws IOException {
        log.debug("Reading type mapping from: " + reader.getPath() + " -> " + defaultFilePath);
        InputStream stream = reader.getInputStream(defaultFilePath);
        if (stream == null) {
            log.debug("No type mapping found in " + reader.getPath() + " -> " + defaultFilePath);
            return new HashMap<>();
        }
        return new ProvidedTypeStreamReader(stream).getMapping();
    }
}
