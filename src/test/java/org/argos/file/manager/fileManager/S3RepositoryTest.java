package org.argos.file.manager.fileManager;

import org.argos.file.manager.exceptions.BadRequestError;
import org.argos.file.manager.exceptions.InternalServerError;
import org.argos.file.manager.repository.S3Repository;
import org.argos.file.manager.exceptions.NotFoundError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.HttpServerErrorException;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link S3Repository}.
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
     * Test that an empty bucket returns a NotFoundError when listing files.
     */
    @Test
    void testListFiles_EmptyBucket() {
        String projectId = "test-project-id";
        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(Collections.emptyList())
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        Exception exception = assertThrows(NotFoundError.class, () -> s3Repository.listFiles(projectId));
        assertEquals("No files found for project ID: test-project-id", exception.getMessage());
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    /**
     * Test file uploads and ensure proper key generation and response.
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
     * Test file content retrieval and ensure correctness.
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
     * Test upload failure due to an exception thrown by S3.
     */
    @Test
    void testUploadDirectory_ThrowsInternalServerError() throws Exception {
        String projectId = "test-project-id";
        Path tempDir = Files.createTempDirectory("testUploadDirectoryException");
        Path tempFile = Files.createTempFile(tempDir, "testFile", ".txt");
        Files.writeString(tempFile, "Sample content");

        AwsErrorDetails mockErrorDetails = AwsErrorDetails.builder()
                .errorMessage("Upload error")
                .build();

        AwsServiceException mockS3Exception = S3Exception.builder()
                .awsErrorDetails(mockErrorDetails)
                .build();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(mockS3Exception);

        BadRequestError exception = assertThrows(BadRequestError.class, () ->
                s3Repository.uploadDirectory(projectId, tempDir.toString())
        );

        assertEquals("Failed to upload files to S3: Upload error", exception.getMessage());

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        Files.deleteIfExists(tempFile);
        Files.deleteIfExists(tempDir);
    }


    /**
     * Test listing files failure due to an S3 exception.
     */
    @Test
    void testListFiles_ThrowsInternalServerError() {
        String projectId = "test-project-id";

        AwsErrorDetails errorDetails = AwsErrorDetails.builder()
                .errorMessage("List error")
                .build();

        AwsServiceException mockS3Exception = S3Exception.builder()
                .awsErrorDetails(errorDetails)
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(mockS3Exception);

        InternalServerError exception = assertThrows(
                InternalServerError.class,
                () -> s3Repository.listFiles(projectId)
        );

        assertEquals("Failed to list files: List error", exception.getMessage());
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
    }



    /**
     * Test retrieving file content failure due to a missing file.
     */
    @Test
    void testGetFileContent_ThrowsNotFoundError() {
        String projectId = "test-project-id";
        String filePath = "missingFile.txt";

        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("File not found").build());

        Exception exception = assertThrows(NotFoundError.class, () -> s3Repository.getFileContent(projectId, filePath));
        assertEquals("File not found: missingFile.txt", exception.getMessage());

        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
    }
}
