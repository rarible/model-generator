package com.rarible.protocol.generator.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rarible.protocol.generator.plugin.config.TaskConfig;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.List;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ModelGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter
    private List<TaskConfig> tasks;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute() throws MojoExecutionException {
        for (TaskConfig task : tasks) {
            try {
                ModelGeneratorTask modelGeneratorTask = new ModelGeneratorTask(getLog(), project, task);
                modelGeneratorTask.execute(task);
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

}
