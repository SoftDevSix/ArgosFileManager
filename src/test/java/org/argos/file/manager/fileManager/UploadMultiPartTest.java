package org.argos.file.manager.fileManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.argos.file.manager.exceptions.*;
import org.argos.file.manager.repository.IStorageRepository;
import org.argos.file.manager.service.S3FileService;
import org.argos.file.manager.utils.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class UploadMultiPartTest {

    private FileProcessor fileProcessorMock;
    private InputValidator inputValidatorMock;
    private IStorageRepository storageRepositoryMock;
    private S3FileService s3FileService;

    @BeforeEach
    void setUp() {
        fileProcessorMock = mock(FileProcessor.class);
        inputValidatorMock = mock(InputValidator.class);
        storageRepositoryMock = mock(IStorageRepository.class);
        s3FileService = new S3FileService(storageRepositoryMock);
    }

    @Test
    void testValidateMultipartFile_validFile() {
        MockMultipartFile validFile =
                new MockMultipartFile("file", "test.zip", "application/zip", new byte[] {1, 2, 3});
        assertDoesNotThrow(() -> inputValidatorMock.validateMultipartFile(validFile));
    }

    @Test
    void testValidateMultipartFile_nullFile_throwsException() {
        MockMultipartFile nullFile = null;

        doThrow(new BadRequestError("Uploaded ZIP file is null or empty."))
                .when(storageRepositoryMock)
                .uploadMultiPartDirectory(anyString(), eq(nullFile));

        BadRequestError exception =
                assertThrows(BadRequestError.class, () -> s3FileService.uploadZipFile(nullFile));

        assertEquals("Uploaded ZIP file is null or empty.", exception.getMessage());
        verify(storageRepositoryMock, times(1)).uploadMultiPartDirectory(anyString(), eq(nullFile));
    }

    @Test
    void testProcessAndExtractZip_validFile() throws IOException {
        MockMultipartFile zipFile =
                new MockMultipartFile("file", "test.zip", "application/zip", new byte[] {1, 2, 3});
        Path tempDir = Files.createTempDirectory("test-unpack");
        doReturn(tempDir).when(fileProcessorMock).processAndExtractZip(zipFile);

        Path resultPath = fileProcessorMock.processAndExtractZip(zipFile);

        assertNotNull(resultPath);
        verify(fileProcessorMock, times(1)).processAndExtractZip(zipFile);
    }

    @Test
    void testExtractZip_invalidEntry_throwsException() {
        Path tempDir = mock(Path.class);
        Path mockZip = mock(Path.class);

        doThrow(new BadRequestError("Invalid ZIP entry"))
                .when(fileProcessorMock)
                .extractZip(mockZip, tempDir);

        BadRequestError exception =
                assertThrows(
                        BadRequestError.class,
                        () -> fileProcessorMock.extractZip(mockZip, tempDir));

        assertEquals("Invalid ZIP entry", exception.getMessage());
    }

    @Test
    void testCleanUpTempDirectory_validPath() throws IOException {
        Path tempDir = Files.createTempDirectory("test-cleanup");

        fileProcessorMock.cleanUpTempDirectory(tempDir);

        verify(fileProcessorMock, times(1)).cleanUpTempDirectory(tempDir);
    }

    @Test
    void testUploadMultiPartDirectory_success() {
        MockMultipartFile zipFile =
                new MockMultipartFile("file", "test.zip", "application/zip", new byte[] {1, 2, 3});
        Map<String, String> mockedUploadResults = new HashMap<>();
        mockedUploadResults.put("file1.txt", "Success");
        mockedUploadResults.put("file2.txt", "Success");

        when(storageRepositoryMock.uploadMultiPartDirectory(anyString(), eq(zipFile)))
                .thenReturn(mockedUploadResults);

        Map<String, Object> result = s3FileService.uploadZipFile(zipFile);
        assertNotNull(result);
        Map<String, String> uploadResults = (Map<String, String>) result.get("uploadResults");
        assertEquals(2, uploadResults.size());
        assertEquals("Success", uploadResults.get("file1.txt"));
        assertEquals("Success", uploadResults.get("file2.txt"));

        verify(storageRepositoryMock, times(1)).uploadMultiPartDirectory(anyString(), eq(zipFile));
    }

    @Test
    void testUploadZipFile_failure_invalidZip() {
        MockMultipartFile invalidZipFile =
                new MockMultipartFile("file", "test.zip", "application/zip", new byte[] {});

        doThrow(new BadRequestError("Uploaded ZIP file is null or empty."))
                .when(storageRepositoryMock)
                .uploadMultiPartDirectory(anyString(), eq(invalidZipFile));

        BadRequestError exception =
                assertThrows(
                        BadRequestError.class, () -> s3FileService.uploadZipFile(invalidZipFile));

        assertEquals("Uploaded ZIP file is null or empty.", exception.getMessage());
        verify(storageRepositoryMock, times(1))
                .uploadMultiPartDirectory(anyString(), eq(invalidZipFile));
    }

    @Test
    void testUploadMultiPartDirectory_failure_invalidProjectId() {
        String invalidProjectId = null;
        doThrow(new BadRequestError("Invalid project ID."))
                .when(inputValidatorMock)
                .validateProjectId(invalidProjectId);

        mockStatic(InputValidator.class);
        when(InputValidator.getInstance()).thenReturn(inputValidatorMock);

        doThrow(new BadRequestError("Invalid project ID."))
                .when(storageRepositoryMock)
                .uploadMultiPartDirectory(eq(invalidProjectId), any(MultipartFile.class));
        verifyNoInteractions(storageRepositoryMock);
    }
}
