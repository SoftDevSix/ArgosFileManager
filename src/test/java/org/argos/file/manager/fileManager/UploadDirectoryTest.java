package org.argos.file.manager.fileManager;

import org.argos.file.manager.exceptions.BadRequestError;
import org.argos.file.manager.repository.S3Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link S3Repository#uploadDirectory(String, String)} method.
 * These tests cover scenarios including successful uploads, invalid inputs,
 * and handling of exceptions during the directory upload process.
 */
class UploadDirectoryTest {

    private S3Client mockS3Client;
    private S3Repository s3Repository;

    /**
     * Set up the test environment before each test.
     * Creates a mock S3Client and initializes the S3Repository.
     */
    @BeforeEach
    void setUp() {
        mockS3Client = mock(S3Client.class);
        s3Repository = new S3Repository(mockS3Client);
    }

    /**
     * Test case for a successful upload of all files in a local directory to S3.
     * Verifies that the correct number of files are uploaded and the S3 client is called the correct number of times.
     *
     * @throws IOException if an I/O error occurs while creating the test files.
     */
    @Test
    void testUploadDirectory_Success() throws IOException {
        String projectId = "testProject";
        Path directory = Files.createTempDirectory("testDir");
        Path file1 = Files.createFile(directory.resolve("file1.txt"));
        Path file2 = Files.createFile(directory.resolve("file2.txt"));

        Map<String, String> result = s3Repository.uploadDirectory(projectId, directory.toString());

        assertEquals(2, result.size());
        assertTrue(result.containsKey("projects/testProject/file1.txt"));
        assertTrue(result.containsKey("projects/testProject/file2.txt"));
        verify(mockS3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        Files.deleteIfExists(file1);
        Files.deleteIfExists(file2);
        Files.deleteIfExists(directory);
    }

    /**
     * Test case for an invalid (null) project ID when uploading a directory.
     * Verifies that the correct BadRequestError is thrown with the expected message.
     */
    @Test
    void testUploadDirectory_NullProjectId() {
        String projectId = null;
        String localDir = "/tmp";

        BadRequestError exception = assertThrows(BadRequestError.class,
                () -> s3Repository.uploadDirectory(projectId, localDir));
        assertEquals("Project ID cannot be null or empty.", exception.getMessage());
    }

    /**
     * Test case for a blank project ID when uploading a directory.
     * Verifies that the correct BadRequestError is thrown with the expected message.
     */
    @Test
    void testUploadDirectory_BlankProjectId() {
        String projectId = "   ";
        String localDir = "/tmp";

        BadRequestError exception = assertThrows(BadRequestError.class,
                () -> s3Repository.uploadDirectory(projectId, localDir));
        assertEquals("Project ID cannot be null or empty.", exception.getMessage());
    }

    /**
     * Test case for an invalid local directory path when uploading files to S3.
     * Verifies that the correct BadRequestError is thrown with the expected message.
     */
    @Test
    void testUploadDirectory_InvalidDirectory() {
        String projectId = "testProject";
        String localDir = "/invalidDir";

        BadRequestError exception = assertThrows(BadRequestError.class,
                () -> s3Repository.uploadDirectory(projectId, localDir));
        assertTrue(exception.getMessage().contains("Invalid local directory:"));
    }

    /**
     * Test case for an empty directory. It verifies that if the directory has no files to upload,
     * a BadRequestError is thrown indicating no files found.
     *
     * @throws IOException if an I/O error occurs during the creation of the temporary directory.
     */
    @Test
    void testUploadDirectory_EmptyDirectory() throws IOException {
        String projectId = "testProject";
        Path emptyDir = Files.createTempDirectory("emptyDir");

        try {
            assertThrowsExactly(BadRequestError.class, (() -> uploadDirectoryAndThrow(projectId, emptyDir.toString())));
        } finally {
            Files.deleteIfExists(emptyDir);
        }
    }

    /**
     * Helper method to upload a directory and throw any exceptions.
     * This is used for testing scenarios where exceptions are expected.
     *
     * @param projectId the ID of the project.
     * @param directoryPath the path to the local directory.
     * @throws BadRequestError if the upload fails.
     */
    private void uploadDirectoryAndThrow(String projectId, String directoryPath) {
        s3Repository.uploadDirectory(projectId, directoryPath);
    }

    /**
     * Test case for an IOException occurring while reading the directory.
     * This simulates an error during the file walking process and verifies that the appropriate error is thrown.
     */
    @Test
    void testUploadDirectory_IOException() {
        String projectId = "testProject";
        Path directory = mock(Path.class);

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(directory)).thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(directory)).thenReturn(true);
            mockedFiles.when(() -> Files.walk(directory)).thenThrow(new IOException("Simulated IO error"));

            assertThrowsExactly(BadRequestError.class, (() -> uploadDirectoryAndThrow(projectId, directory.toString())));
        }
    }
}
