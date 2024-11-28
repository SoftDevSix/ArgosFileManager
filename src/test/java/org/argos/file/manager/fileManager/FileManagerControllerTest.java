package org.argos.file.manager.fileManager;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import org.argos.file.manager.controller.FileManagerController;
import org.argos.file.manager.service.S3FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for the {@link FileManagerController}.
 *
 * This class tests the controller methods related to file management,
 * including listing files, retrieving file content, and uploading directories.
 */
@WebMvcTest(FileManagerController.class)
class FileManagerControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private S3FileService s3FileService;

    /**
     * Tests the endpoint for listing files for a specific project.
     */
    @Test
    void testListFiles() throws Exception {
        String projectId = "test-project-id";
        when(s3FileService.listFiles(projectId)).thenReturn(List.of("file1.java", "file2.java"));

        mockMvc.perform(get("/fileManager/files").param("projectId", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("file1.java"))
                .andExpect(jsonPath("$[1]").value("file2.java"));

        verify(s3FileService, times(1)).listFiles(projectId);
    }

    /**
     * Tests the endpoint for retrieving the content of a specific file for a specific project.
     */
    @Test
    void testGetFileContent() throws Exception {
        String projectId = "test-project-id";
        String filePath = "file1.java";
        String fileContent = "This is a test file.";
        when(s3FileService.getFileContent(projectId, filePath)).thenReturn(fileContent);

        mockMvc.perform(
                        get("/fileManager/file")
                                .param("projectId", projectId)
                                .param("filePath", filePath))
                .andExpect(status().isOk())
                .andExpect(content().string(fileContent));

        verify(s3FileService, times(1)).getFileContent(projectId, filePath);
    }

    /**
     * Tests the endpoint for uploading a directory of files with a generated project ID.
     */
    @Test
    void testUploadDirectory() throws Exception {
        String generatedProjectId = "generated-project-id";
        Map<String, String> uploadResult =
                Map.of(
                        "projectFiles/subdirectory/subClass1.java", "Uploaded",
                        "projectFiles/sec/classDirSec.java", "Uploaded",
                        "projectFiles/directoryFirst/class1.java", "Uploaded");
        when(s3FileService.uploadDirectory("test/"))
                .thenReturn(
                        Map.of(
                                "projectId", generatedProjectId,
                                "uploadResults", uploadResult));

        mockMvc.perform(post("/fileManager/upload").param("localDir", "test/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(generatedProjectId))
                .andExpect(
                        jsonPath("$.uploadResults['projectFiles/subdirectory/subClass1.java']")
                                .value("Uploaded"))
                .andExpect(
                        jsonPath("$.uploadResults['projectFiles/sec/classDirSec.java']")
                                .value("Uploaded"))
                .andExpect(
                        jsonPath("$.uploadResults['projectFiles/directoryFirst/class1.java']")
                                .value("Uploaded"));

        verify(s3FileService, times(1)).uploadDirectory("test/");
    }
}
