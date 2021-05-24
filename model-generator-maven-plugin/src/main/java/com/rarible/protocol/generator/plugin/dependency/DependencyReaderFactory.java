package com.rarible.protocol.generator.plugin.dependency;

import java.io.IOException;

public class DependencyReaderFactory {

    public static DependencyReader getReader(String dependencyPath) throws IOException {
        if (dependencyPath.endsWith(".jar")) {
            return new JarDependencyReader(dependencyPath);
        } else {
            return new DirectoryDependencyReader(dependencyPath);
        }
    }

}
