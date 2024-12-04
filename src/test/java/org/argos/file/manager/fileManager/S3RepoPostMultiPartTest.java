package org.argos.file.manager.fileManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.*;
import java.util.*;

import org.argos.file.manager.exceptions.BadRequestError;
import org.argos.file.manager.repository.S3Repository;
import org.argos.file.manager.utils.FileProcessor;
import org.argos.file.manager.utils.InputValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


class S3RepoPostMultiPartTest {

    private S3Client s3Client;
    private MultipartFile zipFile;
    private S3Repository s3Repository;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        zipFile = mock(MultipartFile.class);
        s3Repository = new S3Repository(s3Client);
    }

    @Test
    void uploadMultiPartDirectory_validInput_shouldUploadFiles() throws Exception {
        String projectId = "test-project";
        Path tempDir = Files.createTempDirectory("tempDir");
        Path file1 = Files.createFile(tempDir.resolve("file1.txt"));
        Path file2 = Files.createFile(tempDir.resolve("file2.txt"));
        List<Path> files = List.of(file1, file2);

        try (MockedStatic<InputValidator> mockedInputValidator = Mockito.mockStatic(InputValidator.class);
             MockedStatic<FileProcessor> mockedFileProcessor = Mockito.mockStatic(FileProcessor.class)) {

            InputValidator inputValidatorMock = mock(InputValidator.class);
            FileProcessor fileProcessorMock = mock(FileProcessor.class);

            mockedInputValidator.when(InputValidator::getInstance).thenReturn(inputValidatorMock);
            mockedFileProcessor.when(FileProcessor::getInstance).thenReturn(fileProcessorMock);

            doNothing().when(inputValidatorMock).validateProjectId(projectId);
            doNothing().when(inputValidatorMock).validateMultipartFile(zipFile);

            when(fileProcessorMock.processAndExtractZip(zipFile)).thenReturn(tempDir);
            when(fileProcessorMock.getFilesFromDirectory(tempDir)).thenReturn(files);
            doNothing().when(fileProcessorMock).validateFilesExist(files);

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);
            Map<String, String> result = s3Repository.uploadMultiPartDirectory(projectId, zipFile);

            assertEquals(2, result.size());
            assertEquals("Uploaded", result.get("projects/test-project/file1.txt"));
            assertEquals("Uploaded", result.get("projects/test-project/file2.txt"));

            verify(fileProcessorMock).cleanUpTempDirectory(tempDir);
        } finally {
            Files.deleteIfExists(file1);
            Files.deleteIfExists(file2);
            Files.deleteIfExists(tempDir);
        }
    }



    @Test
    void uploadMultiPartDirectory_invalidZipFile_shouldThrowBadRequestError() {
        String projectId = "test-project";

        try (MockedStatic<InputValidator> mockedInputValidator = Mockito.mockStatic(InputValidator.class)) {
            InputValidator inputValidatorMock = mock(InputValidator.class);

            mockedInputValidator.when(InputValidator::getInstance).thenReturn(inputValidatorMock);
            doThrow(new BadRequestError("Invalid file"))
                    .when(inputValidatorMock)
                    .validateMultipartFile(zipFile);

            BadRequestError exception = assertThrows(
                    BadRequestError.class,
                    () -> s3Repository.uploadMultiPartDirectory(projectId, zipFile));

            assertEquals("Invalid file", exception.getMessage());
        }
    }
}
