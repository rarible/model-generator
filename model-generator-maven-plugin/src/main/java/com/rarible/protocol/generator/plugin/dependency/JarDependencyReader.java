package com.rarible.protocol.generator.plugin.dependency;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class JarDependencyReader implements DependencyReader {

    private JarFile jarFile;

    public JarDependencyReader(String jarPath) throws IOException {
        this.jarFile = new JarFile(jarPath);
    }

    @Override
    public String getPath() {
        return jarFile.getName();
    }

    @Override
    public InputStream getInputStream(String path) throws IOException {
        ZipEntry entry = jarFile.getEntry(path);
        if (entry == null) {
            return null;
        }
        return jarFile.getInputStream(entry);
    }

    @Override
    public void close() throws IOException {
        IOUtils.close(jarFile);
    }
}
