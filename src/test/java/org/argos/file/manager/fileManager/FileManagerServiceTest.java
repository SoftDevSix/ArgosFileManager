package org.argos.file.manager.fileManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;
import java.util.Map;
import org.argos.file.manager.repository.IStorageRepository;
import org.argos.file.manager.service.S3FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test suite for the FileManagerService class.
 * This suite verifies the behavior of methods related to file management
 * in the S3FileService and its interaction with the IStorageRepository.
 */
class FileManagerServiceTest {

    private IStorageRepository storageRepository;
    private S3FileService s3FileService;

    /**
     * Sets up the test environment by creating a mock implementation
     * of IStorageRepository and initializing the S3FileService with it.
     */
    @BeforeEach
    void setUp() {
        storageRepository = Mockito.mock(IStorageRepository.class);
        s3FileService = new S3FileService(storageRepository);
    }

    /**
     * Verifies that the uploadDirectory method correctly interacts with
     * the storage repository and returns the expected results.
     */
    @Test
    void testUploadDirectory() {
        String localDir = "test-directory";
        Map<String, String> uploadResults =
                Map.of(
                        "file1.txt", "Uploaded",
                        "file2.txt", "Uploaded");

        Mockito.when(storageRepository.uploadDirectory(any(String.class), eq(localDir)))
                .thenReturn(uploadResults);

        Map<String, Object> response = s3FileService.uploadDirectory(localDir);

        assertEquals(2, ((Map<?, ?>) response.get("uploadResults")).size());
        assertEquals(uploadResults, response.get("uploadResults"));

        Mockito.verify(storageRepository).uploadDirectory(any(String.class), eq(localDir));
    }

    /**
     * Tests the listFiles method to ensure it retrieves the list of files
     * for a given project ID from the storage repository.
     */
    @Test
    void testListFiles() {
        String projectId = "1234-5678-91011";
        List<String> fileList = List.of("file1.txt", "file2.txt");

        Mockito.when(storageRepository.listFiles(projectId)).thenReturn(fileList);

        List<String> result = s3FileService.listFiles(projectId);

        assertEquals(fileList.size(), result.size());
        assertEquals(fileList, result);

        Mockito.verify(storageRepository).listFiles(projectId);
    }

    /**
     * Tests the getFileContent method to verify that the file content
     * is correctly retrieved for a given project ID and file path.
     */
    @Test
    void testGetFileContent() {
        String projectId = "1234-5678-91011";
        String filePath = "file1.txt";
        String fileContent = "This is the content of the file.";

        Mockito.when(storageRepository.getFileContent(projectId, filePath)).thenReturn(fileContent);

        String result = s3FileService.getFileContent(projectId, filePath);

        assertEquals(fileContent, result);
        Mockito.verify(storageRepository).getFileContent(projectId, filePath);
    }
}
