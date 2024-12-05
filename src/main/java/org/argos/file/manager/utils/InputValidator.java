package org.argos.file.manager.utils;

import java.nio.file.*;
import org.argos.file.manager.exceptions.BadRequestError;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class for input validation using the Singleton pattern.
 * This class validates input such as project ID, directory paths, and file paths.
 */
@SuppressWarnings("java:S6548")
public class InputValidator {

    /**
     * Private constructor to prevent instantiation from outside the class.
     */
    private InputValidator() {}

    /**
     * Bill Pugh Singleton Design for thread-safe and efficient lazy initialization.
     * The instance is created when it is first used.
     */
    private static class SingletonHelper {
        private static final InputValidator INSTANCE = new InputValidator();
    }

    /**
     * Returns the single instance of the InputValidator class.
     * This method provides a thread-safe and lazy initialization of the instance.
     *
     * @return the singleton instance of InputValidator.
     */
    public static InputValidator getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * Validates that the given project ID is not null or empty.
     *
     * @param projectId the project ID to validate.
     * @throws BadRequestError if the project ID is invalid.
     */
    public void validateProjectId(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new BadRequestError("Project ID cannot be null or empty.");
        }
    }

    /**
     * Validates the given local directory path.
     *
     * @param localDir the directory path to validate.
     * @return the validated directory as a Path object.
     * @throws BadRequestError if the directory is invalid.
     */
    public Path validateDirectory(String localDir) {
        Path directory = Paths.get(localDir);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new BadRequestError("Invalid local directory: " + localDir);
        }
        return directory;
    }

    /**
     * Validates that the given file path is not null or empty.
     *
     * @param filePath the file path to validate.
     * @throws BadRequestError if the file path is invalid.
     */
    public void validateFilePath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new BadRequestError("File path cannot be null or empty.");
        }
    }

    /**
     * Validates the provided {@link MultipartFile}.
     *
     * @param file the {@link MultipartFile} to validate.
     * @throws BadRequestError if the file is null or empty.
     */
    public void validateMultipartFile(MultipartFile file) {
        if (file == null) {
            throw new BadRequestError("Uploaded ZIP file is null.");
        }
    }
}
