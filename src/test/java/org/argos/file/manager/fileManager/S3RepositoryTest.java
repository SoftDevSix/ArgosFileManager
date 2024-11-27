package org.argos.file.manager.fileManager;

import org.argos.file.manager.repository.S3Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link S3Repository}.
 *
 * This class tests the repository methods related to interacting with AWS S3,
 * including listing files in the bucket.
 */
class S3RepositoryTest {

    @Mock
    private S3Client s3Client;

    private S3Repository s3Repository;

    /**
     * Initializes the mock dependencies and the repository instance before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        s3Repository = new S3Repository(s3Client);
    }

    /**
     * Tests the method for listing files when the S3 bucket is empty.
     *
     * This test mocks the response from S3 to return an empty list of objects,
     * and ensures that the repository returns an empty list.
     */
    @Test
    void testListFiles_EmptyBucket() {
        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(Collections.emptyList())
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        List<String> files = s3Repository.listFiles();
        assertTrue(files.isEmpty());
    }

    /**
     * Tests the method for listing files when there are files in the S3 bucket.
     *
     * This test mocks the response from S3 to return a list of files, and ensures
     * that the repository returns the correct list of file names.
     */
    @Test
    void testListFiles_WithFiles() {
        S3Object file1 = S3Object.builder().key("file1.txt").build();
        S3Object file2 = S3Object.builder().key("file2.txt").build();
        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(List.of(file1, file2))
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        List<String> files = s3Repository.listFiles();
        assertEquals(2, files.size());
        assertTrue(files.contains("file1.txt"));
        assertTrue(files.contains("file2.txt"));
    }
}
