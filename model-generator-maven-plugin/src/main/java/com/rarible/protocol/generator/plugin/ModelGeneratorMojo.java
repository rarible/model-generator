package com.rarible.protocol.generator.plugin;

import com.rarible.protocol.generator.plugin.config.DependencyConfig;
import com.rarible.protocol.generator.plugin.config.GeneratorConfig;
import com.rarible.protocol.generator.plugin.config.SchemaConfig;
import com.rarible.protocol.generator.plugin.dependency.SchemaDependencyManager;
import com.rarible.protocol.generator.plugin.mapper.TypeMapperRegistry;
import com.rarible.protocol.generator.plugin.mapper.TypeMapperSettings;
import com.rarible.protocol.merger.RegexFieldNameProcessor;
import com.rarible.protocol.merger.SchemaFieldNameProcessor;
import com.rarible.protocol.merger.SchemaMerger;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ModelGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter
    private SchemaConfig schema;

    @Parameter
    private List<DependencyConfig> dependencies = new ArrayList<>();

    @Parameter
    private List<GeneratorConfig> generators;

    @Parameter
    private String apiPathReplacement;

    @Parameter
    private String apiPathReplacementRegex;

    private Log log;
    private TypeMapperSettings typeMapperSettings;
    private SchemaDependencyManager schemaDependencyManager;

    private File schemaInputFile;
    private File schemaOutputFile;

    @Override
    public void execute() throws MojoExecutionException {
        log = getLog();
        typeMapperSettings = TypeMapperRegistry.getTypeMapperSettings(schema.getType(), getFieldNameProcessor());
        try {
            initDependencies();
            initSchemaFiles();
            mergeSchemas();
            executeTasks();
        } catch (IOException e) {
            throw new MojoExecutionException("Model generation failed: " + e.getMessage(), e);
        }
    }

    private SchemaFieldNameProcessor getFieldNameProcessor() {
        if (StringUtils.isBlank(apiPathReplacementRegex)) {
            return SchemaFieldNameProcessor.Companion.getNO_OP();
        }

        String replacement = apiPathReplacement == null ? "" : apiPathReplacement;
        log.info("API paths will be replaced by regex '" + apiPathReplacementRegex +
                "' with '" + replacement + "'");

        return new RegexFieldNameProcessor(apiPathReplacementRegex, replacement);
    }

    private void initDependencies() throws MojoExecutionException {
        Set<Artifact> artifacts = getDependencies();
        Map<String, Artifact> mapped = artifacts.stream().collect(Collectors.toMap(
                Artifact::getArtifactId,
                (a) -> a,
                // Duplicated jar names possible for some external dependencies we are not interested in
                (a, b) -> a
        ));
        for (DependencyConfig dep : dependencies) {
            Artifact a = mapped.get(dep.getName());
            if (a == null) {
                throw new MojoExecutionException("Dependency jar is not included into project dependencies: " + dep.getName());
            }
            dep.setJarFile(a.getFile().toString());
            log.debug("Dependency found: " + dep.getJarFile());
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

    private void initSchemaFiles() throws MojoExecutionException {
        if (StringUtils.isNotBlank(schema.getInputFile())) {
            schemaInputFile = new File(schema.getInputFile());
            if (!schemaInputFile.exists()) {
                throw new MojoExecutionException("Input schema file not found: " + schemaInputFile);
            }
        } else {
            schemaInputFile = new File(project.getBasedir(), typeMapperSettings.getDefaultSchemaPath());
        }
        log.debug("Input schema file: " + schemaInputFile);
        if (StringUtils.isNotBlank(schema.getOutputFile())) {
            schemaOutputFile = new File(schema.getOutputFile());
        } else {
            schemaOutputFile = new File(Folders.getDefaultClassesFolder(project.getBasedir()), schemaInputFile.getName());
        }
        log.debug("Output schema file: " + schemaOutputFile);
    }

    private void executeTasks() throws MojoExecutionException {
        if (generators == null || generators.isEmpty()) {
            log.info("No source generation tasks defined, skipping this step");
            return;
        }
        for (GeneratorConfig generatorConfig : generators) {
            try {
                ModelGeneratorTask modelGeneratorTask = new ModelGeneratorTask(
                        log,
                        project,
                        schemaDependencyManager,
                        schemaOutputFile,
                        generatorConfig
                );
                modelGeneratorTask.execute();
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    private void mergeSchemas() throws MojoExecutionException, IOException {

        schemaDependencyManager = new SchemaDependencyManager(log, dependencies, typeMapperSettings);

        schemaOutputFile.getParentFile().mkdirs();

        List<String> depSchemaTexts = schemaDependencyManager.getSchemaTexts();
        log.info("Merging " + depSchemaTexts.size() + " schemas into file: " + schemaOutputFile);
        for (String text : depSchemaTexts) {
            log.debug("-----------------------------------");
            log.debug(text);
        }
        SchemaMerger schemaMerger = typeMapperSettings.getSchemaMerger();
        log.debug("Using merger: " + schemaMerger.getClass().getName());
        schemaMerger.mergeSchemas(schemaInputFile, schemaOutputFile, depSchemaTexts);
    }

}
