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
 * This class tests the repository methods related to interacting with AWS S3,
 * including listing files, uploading directories, and retrieving file content.
 */
class S3RepositoryTest {

    @Mock
    private S3Client s3Client;

    private S3Repository s3Repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        s3Repository = new S3Repository(s3Client);
    }

    /**
     * Tests the method for listing files for a specific project when the S3 bucket is empty.
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
     * Tests the method for listing files for a specific project when there are files in the S3 bucket.
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
     * Tests the upload directory method by verifying that files are uploaded with the correct S3 keys.
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
     * Tests the method for retrieving the content of a specific file from S3.
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
}
