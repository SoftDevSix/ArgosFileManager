package org.argos.file.manager.fileManager;

import org.argos.file.manager.exceptions.BadRequestError;
import org.argos.file.manager.exceptions.NotFoundError;
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


class UploadDirectoryTest {

    private S3Client mockS3Client;
    private S3Repository s3Repository;

    @BeforeEach
    void setUp() {
        mockS3Client = mock(S3Client.class);
        s3Repository = new S3Repository(mockS3Client);
    }

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

    @Test
    void testUploadDirectory_NullProjectId() {
        String projectId = null;
        String localDir = "/tmp";

        BadRequestError exception = assertThrows(BadRequestError.class,
                () -> s3Repository.uploadDirectory(projectId, localDir));
        assertEquals("Project ID cannot be null or empty.", exception.getMessage());
    }

    @Test
    void testUploadDirectory_BlankProjectId() {
        String projectId = "   ";
        String localDir = "/tmp";

        BadRequestError exception = assertThrows(BadRequestError.class,
                () -> s3Repository.uploadDirectory(projectId, localDir));
        assertEquals("Project ID cannot be null or empty.", exception.getMessage());
    }

    @Test
    void testUploadDirectory_InvalidDirectory() {
        String projectId = "testProject";
        String localDir = "/invalidDir";

        BadRequestError exception = assertThrows(BadRequestError.class,
                () -> s3Repository.uploadDirectory(projectId, localDir));
        assertTrue(exception.getMessage().contains("Invalid local directory:"));
    }

    @Test
    void testUploadDirectory_EmptyDirectory() throws IOException {
        String projectId = "testProject";
        Path emptyDir = Files.createTempDirectory("emptyDir");

        try {
            assertThrows(NotFoundError.class, () -> uploadDirectoryAndThrow(projectId, emptyDir.toString()));
        } finally {
            Files.deleteIfExists(emptyDir);
        }
    }

    private void uploadDirectoryAndThrow(String projectId, String directoryPath) {
        s3Repository.uploadDirectory(projectId, directoryPath);
    }


    @Test
    void testUploadDirectory_IOException() {
        String projectId = "testProject";
        Path directory = mock(Path.class);

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(directory)).thenReturn(true);
            mockedFiles.when(() -> Files.isDirectory(directory)).thenReturn(true);
            mockedFiles.when(() -> Files.walk(directory)).thenThrow(new IOException("Simulated IO error"));

            assertThrows(BadRequestError.class, () -> uploadDirectoryAndThrow(projectId, directory.toString()));
        }
    }
}
