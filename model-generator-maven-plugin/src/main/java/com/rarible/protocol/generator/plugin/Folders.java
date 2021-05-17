package com.rarible.protocol.generator.plugin;

import java.io.File;

public class Folders {

    private static final String MERGED_PRIMITIVE_TYPES_FILE = "model-generator/merged/%s/primitives.json";
    private static final String MERGED_PROVIDED_TYPES_FILE = "model-generator/merged/%s/provided.json";

    private static final String DEFAULT_PRIMITIVE_TYPES_FILE = "src/main/resources/model-generator/%s/primitives.json";
    private static final String DEFAULT_PROVIDED_TYPES_FILE = "src/main/resources/model-generator/%s/provided.json";

    private static final String GENERATED_CLASSES_OUTPUT_DIR = "target/generated-sources/rarible/src/main/%s/";
    private static final String COMPILED_CLASSES_OUTPUT_DIR = "target/classes";

    public static String getMergedPrimitiveTypesFileRelativePath(String lang) {
        return String.format(MERGED_PRIMITIVE_TYPES_FILE, lang);
    }

    public static String getMergedProvidedTypesFileRelativePath(String lang) {
        return String.format(MERGED_PROVIDED_TYPES_FILE, lang);
    }

    public static File getMergedPrimitiveTypesFilePath(File parent, String lang) {
        return new File(parent, String.format(MERGED_PRIMITIVE_TYPES_FILE, lang));
    }

    public static File getMergedProvidedTypesFilePath(File parent, String lang) {
        return new File(parent, String.format(MERGED_PROVIDED_TYPES_FILE, lang));
    }

    public static String getDefaultPrimitiveTypesFileRelativePath(String lang) {
        return String.format(DEFAULT_PRIMITIVE_TYPES_FILE, lang);
    }

    public static String getDefaultProvidedTypesFileRelativePath(String lang) {
        return String.format(DEFAULT_PROVIDED_TYPES_FILE, lang);
    }

    public static File getDefaultPrimitiveTypesFilePath(File parent, String lang) {
        return new File(parent, String.format(DEFAULT_PRIMITIVE_TYPES_FILE, lang));
    }

    public static File getDefaultProvidedTypesFilePath(File parent, String lang) {
        return new File(parent, String.format(DEFAULT_PROVIDED_TYPES_FILE, lang));
    }

    public static File getDefaultGeneratedSourcesFolder(File parent, String lang) {
        return new File(parent, String.format(GENERATED_CLASSES_OUTPUT_DIR, lang));
    }

    public static File getDefaultClassesFolder(File parent) {
        return new File(parent, COMPILED_CLASSES_OUTPUT_DIR);
    }

}
