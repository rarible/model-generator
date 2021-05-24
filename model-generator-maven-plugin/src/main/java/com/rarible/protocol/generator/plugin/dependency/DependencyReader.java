package com.rarible.protocol.generator.plugin.dependency;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface DependencyReader extends Closeable {

    String getPath();

    InputStream getInputStream(String path) throws IOException;

}
