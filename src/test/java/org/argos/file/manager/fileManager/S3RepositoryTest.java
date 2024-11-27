package org.argos.file.manager.fileManager;

import org.argos.file.manager.repository.S3Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * Unit tests for the {@link S3Repository}.
 *
 * This test class verifies the interaction between {@link S3Repository} and the AWS S3 API.
 * It includes tests for listing files, uploading directories, retrieving file content,
 * and handling exceptions during S3 operations.
 */
class S3RepositoryTest {

    @Mock
    private S3Client s3Client;

    private S3Repository s3Repository;

    /**
     * Initializes mocks and the S3Repository instance before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        s3Repository = new S3Repository(s3Client);
    }

    /**
     * Tests the listFiles method when the S3 bucket is empty.
     * Ensures that the returned list is empty and that the correct S3Client method is called.
     */
    @Test
    void testListFiles_EmptyBucket() {
        String projectId = "test-project-id";
        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(Collections.emptyList())
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        List<String> files = s3Repository.listFiles(projectId);
        assertTrue(files.isEmpty());
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    /**
     * Tests the listFiles method when files are present in the S3 bucket.
     * Verifies that the correct file paths are returned.
     */
    @Test
    void testListFiles_WithFiles() {
        String projectId = "test-project-id";
        S3Object file1 = S3Object.builder().key("projects/test-project-id/file1.txt").build();
        S3Object file2 = S3Object.builder().key("projects/test-project-id/file2.txt").build();
        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(List.of(file1, file2))
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        List<String> files = s3Repository.listFiles(projectId);
        assertEquals(2, files.size());
        assertTrue(files.contains("projects/test-project-id/file1.txt"));
        assertTrue(files.contains("projects/test-project-id/file2.txt"));
    }

    /**
     * Tests the uploadDirectory method.
     * Verifies that files are uploaded with correct keys and checks the upload results.
     */
    @Test
    void testUploadDirectory() throws Exception {
        String projectId = "test-project-id";
        Path tempDir = Files.createTempDirectory("testUploadDirectory");
        Path tempFile = Files.createTempFile(tempDir, "testFile", ".txt");
        Files.writeString(tempFile, "Sample content");

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(mock(PutObjectResponse.class));

        Map<String, String> result = s3Repository.uploadDirectory(projectId, tempDir.toString());
        String expectedKey = String.format("projects/%s/%s", projectId, tempDir.relativize(tempFile).toString().replace("\\", "/"));
        assertTrue(result.containsKey(expectedKey));
        assertEquals("Uploaded", result.get(expectedKey));

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        Files.deleteIfExists(tempFile);
        Files.deleteIfExists(tempDir);
    }

    /**
     * Tests the getFileContent method.
     * Verifies that the correct content is retrieved for the specified file.
     */
    @Test
    void testGetFileContent() {
        String projectId = "test-project-id";
        String filePath = "file1.txt";
        String fileContent = "This is a test file.";
        String key = String.format("projects/%s/%s", projectId, filePath);

        GetObjectRequest expectedRequest = GetObjectRequest.builder()
                .bucket("group8-image-uploader-s3")
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> mockResponseInputStream = mock(ResponseInputStream.class);

        try {
            when(mockResponseInputStream.readAllBytes()).thenReturn(fileContent.getBytes());
        } catch (Exception e) {
            fail("Unexpected exception while mocking ResponseInputStream: " + e.getMessage());
        }

        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenAnswer(invocation -> {
                    GetObjectRequest actualRequest = invocation.getArgument(0);
                    assertEquals(expectedRequest.bucket(), actualRequest.bucket());
                    assertEquals(expectedRequest.key(), actualRequest.key());
                    return mockResponseInputStream;
                });

        String result = s3Repository.getFileContent(projectId, filePath);

        assertEquals(fileContent, result);
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
    }

    /**
     * Tests exception handling in the listFiles method.
     * Verifies that an error message is returned when S3 throws an exception.
     */
    @Test
    void testListFiles_ThrowsException() {
        String projectId = "test-project-id";
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        List<String> files = s3Repository.listFiles(projectId);

        assertTrue(files.contains("Error fetching file list: S3 error"));
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    /**
     * Tests exception handling in the uploadDirectory method.
     * Verifies that an error message is included in the result map when S3 throws an exception.
     */
    @Test
    void testUploadDirectory_ThrowsException() throws Exception {
        String projectId = "test-project-id";
        Path tempDir = Files.createTempDirectory("testUploadDirectoryException");
        Path tempFile = Files.createTempFile(tempDir, "testFile", ".txt");
        Files.writeString(tempFile, "Sample content");

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("Upload error").build());

        Map<String, String> result = s3Repository.uploadDirectory(projectId, tempDir.toString());

        assertTrue(result.containsKey("error"));
        assertEquals("Failed to upload files: Upload error", result.get("error"));

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        Files.deleteIfExists(tempFile);
        Files.deleteIfExists(tempDir);
    }

    /**
     * Tests exception handling in the getFileContent method.
     * Verifies that an error message is returned when S3 throws an exception.
     */
    @Test
    void testGetFileContent_ThrowsException() {
        String projectId = "test-project-id";
        String filePath = "file1.txt";

        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("GetObject error").build());

        String result = s3Repository.getFileContent(projectId, filePath);

        assertEquals("Failed to retrieve file: GetObject error", result);

        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
    }
}
