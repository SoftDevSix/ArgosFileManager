package org.argos.file.manager.fileManager;

import org.argos.file.manager.controller.FileManagerController;
import org.argos.file.manager.service.S3FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the {@link FileManagerController}.
 *
 * This class tests the controller methods related to file management,
 * including listing files, retrieving file content, and uploading directories.
 */
@WebMvcTest(FileManagerController.class)
class FileManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3FileService s3FileService;

    /**
     * Tests the endpoint for listing files.
     *
     * This test mocks the response from {@link S3FileService} to return a list of files
     * and checks that the controller returns the correct JSON response.
     */
    @Test
    void testListFiles() throws Exception {
        when(s3FileService.listFiles()).thenReturn(List.of("file1.java", "file2.java"));

        mockMvc.perform(get("/api/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("file1.java"))
                .andExpect(jsonPath("$[1]").value("file2.java"));

        verify(s3FileService, times(1)).listFiles();
    }

    /**
     * Tests the endpoint for retrieving the content of a specific file.
     *
     * This test mocks the response from {@link S3FileService} to return the content of a file
     * and ensures the controller returns the expected content in the response.
     */
    @Test
    void testGetFileContent() throws Exception {
        String fileKey = "file1.java";
        String fileContent = "This is a test file.";
        when(s3FileService.getFileContent(fileKey)).thenReturn(fileContent);

        mockMvc.perform(get("/api/file").param("key", fileKey))
                .andExpect(status().isOk())
                .andExpect(content().string(fileContent));

        verify(s3FileService, times(1)).getFileContent(fileKey);
    }

    /**
     * Tests the endpoint for uploading a directory of files.
     *
     * This test mocks the response from {@link S3FileService} to simulate the upload
     * of files in a directory and checks that the controller returns the correct status and JSON response.
     */
    @Test
    void testUploadDirectory() throws Exception {
        Map<String, String> uploadResult = Map.of(
                "projectFiles/subdirector/subClass1.java", "Uploaded",
                "projectFiles/sec/classDirSec.java", "Uploaded",
                "projectFiles/dirctoryFirst/class1.java", "Uploaded"
        );
        String localDir = "test/";
        when(s3FileService.uploadDirectory(localDir)).thenReturn(uploadResult);

        mockMvc.perform(post("/api/upload").param("localDir", localDir))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['projectFiles/subdirector/subClass1.java']").value("Uploaded"))
                .andExpect(jsonPath("$['projectFiles/sec/classDirSec.java']").value("Uploaded"))
                .andExpect(jsonPath("$['projectFiles/dirctoryFirst/class1.java']").value("Uploaded"));

        verify(s3FileService, times(1)).uploadDirectory(localDir);
    }

}
