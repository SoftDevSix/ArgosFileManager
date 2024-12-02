package org.argos.file.manager.utils;

import java.nio.file.Path;

/**
 * Utility class for generating object keys.
 * This class cannot be instantiated as it contains only static methods.
 */
public class KeyGenerator {

    /**
     * Private constructor to prevent instantiation.
     */
    private KeyGenerator() {}

    /**
     * Generates an object key for the given file.
     *
     * @param projectId the unique identifier for the project.
     * @param directory the root directory of the files being uploaded.
     * @param file the file for which the key is generated.
     * @return the generated object key.
     */
    public static String generateKey(String projectId, Path directory, Path file) {
        return String.format(
                "projects/%s/%s",
                projectId, directory.relativize(file).toString().replace("\\", "/"));
    }
}
