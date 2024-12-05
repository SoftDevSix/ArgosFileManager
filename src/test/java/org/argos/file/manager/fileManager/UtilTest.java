package org.argos.file.manager.fileManager;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.argos.file.manager.exceptions.BadRequestError;
import org.argos.file.manager.utils.FileProcessor;
import org.argos.file.manager.utils.InputValidator;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;

class UtilTest {

    private FileProcessor fileProcessor;
    private InputValidator inputValidator;

    @BeforeEach
    void setUp() {
        fileProcessor = FileProcessor.getInstance();
        inputValidator = InputValidator.getInstance();
    }

    @Test
    void testExtractZip() throws IOException {
        Path tempDir = Files.createTempDirectory("test-extract-zip");
        Path zipFile = tempDir.resolve("test.zip");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            zos.write("Sample content".getBytes());
            zos.closeEntry();
        }

        Path targetDir = tempDir.resolve("output");
        Files.createDirectories(targetDir);

        fileProcessor.extractZip(zipFile, targetDir);

        Path extractedFile = targetDir.resolve("test.txt");
        assertTrue(Files.exists(extractedFile));
        assertEquals("Sample content", Files.readString(extractedFile));

        fileProcessor.cleanUpTempDirectory(tempDir);
    }

    @Test
    void testProcessAndExtractZip() throws IOException {
        MockMultipartFile mockFile =
                new MockMultipartFile("file", "test.zip", "application/zip", createZipContent());

        Path extractedDir = fileProcessor.processAndExtractZip(mockFile);

        Path extractedFile = extractedDir.resolve("test.txt");
        assertTrue(Files.exists(extractedFile));
        assertEquals("Sample content", Files.readString(extractedFile));

        fileProcessor.cleanUpTempDirectory(extractedDir);
    }

    @Test
    void testCleanUpTempDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("test-cleanup");
        Files.createFile(tempDir.resolve("tempfile.txt"));

        assertTrue(Files.exists(tempDir));

        fileProcessor.cleanUpTempDirectory(tempDir);

        assertFalse(Files.exists(tempDir));
    }

    @Test
    void testDeleteDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("test-delete-dir");
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createDirectories(tempDir.resolve("subdir"));
        Files.createFile(tempDir.resolve("subdir/file2.txt"));

        fileProcessor.deleteDirectory(tempDir);

        assertFalse(Files.exists(tempDir));
    }

    private byte[] createZipContent() throws IOException {
        Path tempZip = Files.createTempFile("temp", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tempZip))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            zos.write("Sample content".getBytes());
            zos.closeEntry();
        }
        byte[] data = Files.readAllBytes(tempZip);
        Files.delete(tempZip);
        return data;
    }

    @Test
    void testValidateFilePathWithValidPath() {
        String filePath = "valid/path/to/file";

        assertDoesNotThrow(() -> inputValidator.validateFilePath(filePath));
    }

    @Test
    void testValidateMultipartFileWithValidFile() {
        MockMultipartFile validFile =
                new MockMultipartFile("file", "test.zip", "application/zip", "content".getBytes());

        assertDoesNotThrow(() -> inputValidator.validateMultipartFile(validFile));
    }

    @Test
    void testValidateAndResolvePathWithValidEntry() throws IOException {
        Path targetDir = Files.createTempDirectory("valid-target");
        ZipEntry validEntry = new ZipEntry("valid/path/to/file.txt");

        Path resolvedPath = fileProcessor.validateAndResolvePath(validEntry, targetDir);

        assertTrue(resolvedPath.startsWith(targetDir));
        Files.deleteIfExists(resolvedPath);
        Files.delete(targetDir);
    }

    @Test
    void testValidateAndResolvePathWithInvalidEntryOutsideTargetDir() {
        Path targetDir = Paths.get("/valid/target");
        ZipEntry invalidEntry = new ZipEntry("../outside/path/to/file.txt");

        BadRequestError exception =
                assertThrows(
                        BadRequestError.class,
                        () -> {
                            fileProcessor.validateAndResolvePath(invalidEntry, targetDir);
                        });

        assertEquals("Invalid ZIP entry: ../outside/path/to/file.txt", exception.getMessage());
    }

    @Test
    void testValidateAndResolvePathWithSymbolicLinkEntry() throws IOException {
        Path targetDir = Files.createTempDirectory("symbolic-target");
        Path symlinkTarget = Files.createFile(targetDir.resolve("symlinkTarget"));
        Path symlink = Files.createSymbolicLink(targetDir.resolve("symlink.txt"), symlinkTarget);

        ZipEntry symlinkEntry = new ZipEntry("symlink.txt");

        BadRequestError exception =
                assertThrows(
                        BadRequestError.class,
                        () -> {
                            fileProcessor.validateAndResolvePath(symlinkEntry, targetDir);
                        });

        assertEquals("ZIP entry contains a symbolic link: symlink.txt", exception.getMessage());

        Files.delete(symlink);
        Files.delete(symlinkTarget);
        Files.delete(targetDir);
    }
}
