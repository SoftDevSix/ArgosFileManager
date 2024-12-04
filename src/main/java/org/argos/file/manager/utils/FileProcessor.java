package org.argos.file.manager.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.argos.file.manager.exceptions.BadRequestError;
import org.argos.file.manager.exceptions.NotFoundError;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * Extracts a ZIP file into the specified target directory.
     *
     * @param zipFilePath the path to the ZIP file to extract.
     * @param targetDir the directory to extract the contents into.
     * @throws BadRequestError if the ZIP file cannot be processed.
     */
    @SuppressWarnings("java:S5042")
    public void extractZip(Path zipFilePath, Path targetDir) {
        try (ZipInputStream zipInputStream =
                new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path extractedPath = targetDir.resolve(entry.getName()).normalize();
                if (!extractedPath.startsWith(targetDir)) {
                    throw new IOException(
                            "Potential directory traversal attempt in ZIP entry: "
                                    + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(extractedPath);
                } else {
                    Files.createDirectories(extractedPath.getParent());
                    Files.copy(zipInputStream, extractedPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new BadRequestError("Error extracting ZIP file: " + e.getMessage());
        }
    }

    /**
     * Validates the extracted path to ensure it is within the target directory and not
     * a directory traversal attack (e.g., `../../some/path`).
     *
     * @param entry the ZIP entry to validate.
     * @param targetDir the target directory to extract files into.
     * @return the validated path for the extracted file.
     * @throws BadRequestError if the path is invalid.
     */
    public Path validateAndResolvePath(ZipEntry entry, Path targetDir) {
        Path resolvedPath = targetDir.resolve(entry.getName());

        if (!resolvedPath.normalize().startsWith(targetDir)) {
            throw new BadRequestError("Invalid ZIP entry: " + entry.getName());
        }
        if (Files.isSymbolicLink(resolvedPath)) {
            throw new BadRequestError("ZIP entry contains a symbolic link: " + entry.getName());
        }

        return resolvedPath;
    }

    /**
     * Processes the given MultipartFile, creates a temporary directory,
     * and extracts the ZIP contents into it.
     *
     * @param zipFile the MultipartFile containing the ZIP file.
     * @return the path to the temporary directory containing extracted files.
     */
    @SuppressWarnings("java:S5443")
    public Path processAndExtractZip(MultipartFile zipFile) {
        try {
            Path tempDir = Files.createTempDirectory("unpacked-zip");
            setDirectoryPermissions(tempDir);

            String originalFilename = zipFile.getOriginalFilename();
            String sanitizedFileName =
                    sanitizeFileName(originalFilename != null ? originalFilename : "uploaded.zip");
            Path tempZipPath = tempDir.resolve(sanitizedFileName);

            Files.write(tempZipPath, zipFile.getBytes());

            extractZip(tempZipPath, tempDir);

            return tempDir;
        } catch (IOException e) {
            throw new BadRequestError("Failed to process ZIP file: " + e.getMessage());
        }
    }

    /**
     * Sets restricted permissions for the given directory to prevent unauthorized access.
     *
     * @param dir the directory to set permissions for.
     * @throws IOException if an error occurs while setting permissions.
     */
    private void setDirectoryPermissions(Path dir) throws IOException {
        Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwx------");
        Files.setPosixFilePermissions(dir, permissions);
    }

    /**
     * Sanitizes a file name to avoid potential security risks.
     *
     * @param fileName the original file name.
     * @return a sanitized file name.
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
    }

    /**
     * Safely deletes a temporary directory, logging any errors that occur.
     *
     * @param tempDir the directory to clean up.
     */
    public void cleanUpTempDirectory(Path tempDir) {
        if (tempDir != null) {
            try {
                deleteDirectory(tempDir);
            } catch (IOException e) {
                throw new BadRequestError("Failed to clean up temporary files: " + e.getMessage());
            }
        }
    }

    /**
     * Deletes a directory and its contents recursively.
     *
     * @param directory the root directory to delete.
     * @throws IOException if any part of the directory cannot be deleted.
     */
    public void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(
                directory,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
    }
}
