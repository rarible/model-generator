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
import org.apache.commons.io.IOUtils;
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
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class ModelDependencyProvider {

    private Log log;

    private final String jarFile;
    private final String schemaFile;
    private final String defaultSchemaFile;

    private final QualifierGenerator qualifierGenerator;
    private final TypeMapperFactory typeMapperFactory;
    private final GeneratorFactory generatorFactory;

    public ModelDependencyProvider(Log log,
                                   DependencyConfig dependency,
                                   GeneratorFactory generatorFactory,
                                   TypeMapperSettings typeMapperSettings
    ) {
        this.log = log;
        this.jarFile = dependency.getJarFile();
        this.schemaFile = dependency.getSchemaFile();
        this.defaultSchemaFile = typeMapperSettings.getDefaultSchemaPath();
        this.qualifierGenerator = generatorFactory.getQualifierGenerator();
        this.generatorFactory = generatorFactory;
        this.typeMapperFactory = typeMapperSettings.getTypeMapperFactory();
    }

    public ModelDependency getDependency() throws IOException {
        log.debug("Opening jar file: " + jarFile);
        try (JarFile jar = new JarFile(jarFile)) {
            ModelDependency result = new ModelDependency();
            result.setJarFile(jarFile);
            result.setText(readSchemaText(jar));

            Map<String, String> defaultPrimitiveTypes = generatorFactory.getDefaultPrimitiveTypesReader().getMapping();
            Map<String, String> defaultProvidedTypes = generatorFactory.getDefaultProvidedTypesReader().getMapping();

            String lang = generatorFactory.getLang();
            String primitiveTypesFile = Folders.getMergedPrimitiveTypesFileRelativePath(lang);
            String providedTypesFile = Folders.getMergedProvidedTypesFileRelativePath(lang);

            defaultPrimitiveTypes.putAll(readTypes(jar, primitiveTypesFile));
            defaultProvidedTypes.putAll(readTypes(jar, providedTypesFile));
            log.debug("Primitive types (default included): " + defaultPrimitiveTypes);
            log.debug("Provided types (default included): " + defaultProvidedTypes);

            result.setPrimitiveTypes(defaultPrimitiveTypes);
            result.setProvidedTypes(defaultProvidedTypes);

            TypeMapper mapper = typeMapperFactory.getTypeDefinitionMapper(
                    qualifierGenerator,
                    result.getPrimitiveTypes(),
                    result.getProvidedTypes()
            );

            List<GeneratedComponent> providedComponents = readComponents(mapper, result.getText(), defaultSchemaFile);

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
        log.debug("Writing components from schema to the temp file: " + file.toString());
        try {
            FileUtils.write(file, text, StandardCharsets.UTF_8);
            return mapper.readGeneratedComponents(Paths.get(file.toURI()));
        } finally {
            log.debug("Removing temp file " + file);
            file.delete();
        }
    }

    private String readSchemaText(JarFile jar) throws IOException {
        ZipEntry zipEntry;
        if (StringUtils.isNotBlank(schemaFile)) {
            log.debug("Trying to read schema file " + jar.getName() + " -> " + schemaFile);
            zipEntry = jar.getEntry(schemaFile);
            if (zipEntry == null) {
                throw new IOException("Schema file '" + schemaFile + "'" +
                        " not found in jar '" + jar.getName() + "'");
            }
        } else {
            log.debug("Custom schema path not specified, trying to read default schema file: "
                    + jar.getName() + " -> " + defaultSchemaFile);

            zipEntry = jar.getEntry(defaultSchemaFile);
            if (zipEntry == null) {
                throw new IOException("Default schema file '" + defaultSchemaFile + "' not found" +
                        " in jar '" + jar.getName() + "' and no custom path for schema specified");
            }
        }
        try (InputStream stream = jar.getInputStream(zipEntry)) {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> readTypes(JarFile jar, String defaultFilePath) throws IOException {
        log.debug("Reading type mapping from: " + jar.getName() + " -> " + defaultFilePath);
        ZipEntry zipEntry = jar.getEntry(defaultFilePath);
        if (zipEntry == null) {
            log.debug("No type mapping found in " + jar.getName() + " -> " + defaultFilePath);
            return new HashMap<>();
        }
        return readTypes(jar, zipEntry);
    }

    private Map<String, String> readTypes(JarFile jar, ZipEntry zipEntry) throws IOException {
        InputStream stream = jar.getInputStream(zipEntry);
        ProvidedTypeStreamReader reader = new ProvidedTypeStreamReader(stream);
        return reader.getMapping();
    }
}
