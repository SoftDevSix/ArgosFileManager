package org.argos.file.manager.utils;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;
import org.argos.file.manager.exceptions.BadRequestError;
import org.argos.file.manager.exceptions.NotFoundError;

/**
 * Utility class for processing files. This class uses the Singleton pattern to ensure only
 * one instance of the class exists throughout the application.
 */
@SuppressWarnings("java:S6548")
public class FileProcessor {

    /**
     * Private constructor to prevent instantiation from outside the class.
     */
    private FileProcessor() {}

    /**
     * Bill Pugh Singleton Design for thread-safe and efficient lazy initialization.
     * The instance is created when it is first used.
     */
    private static class SingletonHelper {
        private static final FileProcessor INSTANCE = new FileProcessor();
    }

    /**
     * Returns the single instance of the FileProcessor class.
     * This method provides a thread-safe and lazy initialization of the instance.
     *
     * @return the singleton instance of FileProcessor.
     */
    public static FileProcessor getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * Retrieves all regular files from a directory stream.
     *
     * This method walks the provided directory and returns a list of all regular files
     * found within it.
     *
     * @param directory the directory to walk and retrieve files from.
     * @return a list of regular files found within the directory.
     * @throws NotFoundError if the directory cannot be read.
     */
    public List<Path> getFilesFromDirectory(Path directory) {
        try (Stream<Path> stream = Files.walk(directory)) {
            return stream.filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            throw new NotFoundError("Failed to read files from directory: " + e.getMessage());
        }
    }

    /**
     * Validates that the given list of files is not empty.
     *
     * This method checks if the list of files passed is empty, and if so, it throws a BadRequestError
     * with an appropriate error message.
     *
     * @param files the list of files to validate.
     * @throws BadRequestError if the file list is empty.
     */
    public void validateFilesExist(List<Path> files) {
        if (files.isEmpty()) {
            throw new BadRequestError("No files found in the directory to upload.");
        }
    }
}
