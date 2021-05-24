package com.rarible.protocol.generator.plugin.dependency;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DirectoryDependencyReader implements DependencyReader {

    private File directory;

    public DirectoryDependencyReader(String directory) {
        this.directory = new File(directory);
    }

    @Override
    public String getPath() {
        return directory.toString();
    }

    @Override
    public InputStream getInputStream(String path) throws IOException {
        File file = new File(directory, path);
        if (!file.exists()) {
            return null;
        }
        return new FileInputStream(file);
    }

    @Override
    public void close() throws IOException {

    }
}
