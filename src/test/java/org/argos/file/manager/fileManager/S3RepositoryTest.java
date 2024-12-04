package org.argos.file.manager.fileManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import org.argos.file.manager.exceptions.BadRequestError;
import org.argos.file.manager.exceptions.NotFoundError;
import org.argos.file.manager.repository.S3Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

/**
 * Unit tests for the {@link S3Repository}.
 */
class S3RepositoryTest {

    @Mock private S3Client s3Client;

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
        ListObjectsV2Response response =
                ListObjectsV2Response.builder().contents(Collections.emptyList()).build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        Exception exception =
                assertThrows(NotFoundError.class, () -> s3Repository.listFiles(projectId));
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

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(mock(PutObjectResponse.class));

        Map<String, String> result = s3Repository.uploadDirectory(projectId, tempDir.toString());
        String expectedKey =
                String.format(
                        "projects/%s/%s",
                        projectId, tempDir.relativize(tempFile).toString().replace("\\", "/"));
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

        GetObjectRequest expectedRequest =
                GetObjectRequest.builder().bucket("group8-image-uploader-s3").key(key).build();

        ResponseBytes<GetObjectResponse> mockResponseBytes = mock(ResponseBytes.class);

        when(mockResponseBytes.asUtf8String()).thenReturn(fileContent);

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenAnswer(
                        invocation -> {
                            GetObjectRequest actualRequest = invocation.getArgument(0);
                            assertEquals(expectedRequest.key(), actualRequest.key());
                            return mockResponseBytes;
                        });

        String result = s3Repository.getFileContent(projectId, filePath);

        assertEquals(fileContent, result);
        verify(s3Client, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
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

        AwsErrorDetails mockErrorDetails =
                AwsErrorDetails.builder().errorMessage("Upload error").build();
        AwsServiceException mockS3Exception =
                S3Exception.builder().awsErrorDetails(mockErrorDetails).build();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(mockS3Exception);

        BadRequestError exception =
                assertThrows(BadRequestError.class, () -> uploadToS3(projectId, tempDir));

        assertEquals("Failed to upload files to S3: Upload error", exception.getMessage());
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        Files.deleteIfExists(tempFile);
        Files.deleteIfExists(tempDir);
    }

    /**
     * Encapsulates the invocation of the S3 upload logic for testing.
     *
     * @param projectId the project ID
     * @param directoryPath the directory to upload
     * @throws BadRequestError if an error occurs during upload
     */
    private void uploadToS3(String projectId, Path directoryPath) throws BadRequestError {
        s3Repository.uploadDirectory(projectId, directoryPath.toString());
    }

    /**
     * Test listing files failure due to an S3 exception.
     */
    @Test
    void testListFiles_ThrowsBadRequestError() {
        String projectId = "test-project-id";

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(S3Exception.builder().message("Error occurred").build());

        BadRequestError exception =
                assertThrows(BadRequestError.class, () -> s3Repository.listFiles(projectId));

        assertEquals("Failed to list files: Error occurred", exception.getMessage());
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    /**
     * Test retrieving file content failure due to a missing file.
     */
    @Test
    void testGetFileContent_ThrowsNotFoundError() {
        String projectId = "test-project-id";
        String filePath = "missingFile.txt";

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("File not found").build());

        Exception exception =
                assertThrows(
                        NotFoundError.class,
                        () -> s3Repository.getFileContent(projectId, filePath));
        assertEquals("File not found: missingFile.txt", exception.getMessage());

        verify(s3Client, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
    }

    /**
     * Test to receive Bad Request Exception Response
     */
    @Test
    void testGetFileContent_EmptyProjectId_ThrowsBadRequestError() {
        String filePath = "test-file.txt";

        Exception exception =
                assertThrows(
                        BadRequestError.class, () -> s3Repository.getFileContent("", filePath));
        assertEquals("Project ID cannot be null or empty.", exception.getMessage());
    }

    /**
     * Test to receive Bad Request Exception Response with an epmty file path
     */
    @Test
    void testGetFileContent_EmptyFilePath_ThrowsBadRequestError() {
        String projectId = "test-project-id";

        Exception exception =
                assertThrows(
                        BadRequestError.class, () -> s3Repository.getFileContent(projectId, ""));
        assertEquals("File path cannot be null or empty.", exception.getMessage());
    }

    /**
     * Test to receive An error file reading type
     */
    @Test
    void testGetFileContent_ThrowsIOException() {
        String projectId = "test-project-id";
        String filePath = "test-file.txt";

        ResponseBytes<GetObjectResponse> mockResponseBytes = mock(ResponseBytes.class);

        when(mockResponseBytes.asUtf8String()).thenThrow(new RuntimeException("Read error"));

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(mockResponseBytes);

        Exception exception =
                assertThrows(
                        BadRequestError.class,
                        () -> s3Repository.getFileContent(projectId, filePath));

        assertTrue(exception.getMessage().contains("Error reading file content"));
        verify(s3Client, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
    }

    @Test
    void testUploadMultiPartDirectory_EmptyProjectId() {
        String projectId = "";
        MultipartFile zipFile = mock(MultipartFile.class);

        BadRequestError exception = assertThrows(BadRequestError.class,
                () -> s3Repository.uploadMultiPartDirectory(projectId, zipFile));

        assertEquals("Project ID cannot be null or empty.", exception.getMessage());
    }
}
