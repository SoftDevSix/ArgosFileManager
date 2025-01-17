package org.argos.file.manager.controller;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.argos.file.manager.service.S3FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for managing files in AWS S3.
 */
@RestController
@RequestMapping("/fileManager")
@AllArgsConstructor
public class FileManagerController {

    private final S3FileService s3FileService;

    /**
     * Uploads an entire directory to the S3 bucket and generates a new project ID.
     *
     * @param localDir the path to the local directory to upload.
     * @return a map containing the generated project ID and uploaded file statuses.
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadDirectory(@RequestParam String localDir) {
        return s3FileService.uploadDirectory(localDir);
    }

    /**
     * Lists all files in the S3 bucket for a specific project.
     *
     * @param projectId the ID of the project.
     * @return a list of file keys in the S3 bucket for the given project.
     */
    @GetMapping("/files")
    public List<String> listFiles(@RequestParam String projectId) {
        return s3FileService.listFiles(projectId);
    }

    /**
     * Retrieves the content of a specific file from the S3 bucket for a specific project.
     *
     * @param projectId the ID of the project.
     * @param filePath  the relative path of the file to retrieve.
     * @return the content of the file as a string.
     */
    @GetMapping("/file")
    public String getFile(@RequestParam String projectId, @RequestParam String filePath) {
        return s3FileService.getFileContent(projectId, filePath);
    }

    /**
     * Uploads a ZIP file to the S3 bucket, extracts its contents, and organizes them under a new project ID.
     *
     * @param file      the uploaded ZIP file.
     * @return a map containing the generated project ID and uploaded file statuses.
     */
    @PostMapping("/uploadZip")
    public Map<String, Object> uploadZipFile(@RequestParam MultipartFile file) {
        return s3FileService.uploadZipFile(file);
    }
}
