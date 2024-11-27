package org.argos.file.manager.controller;

import org.argos.file.manager.service.S3FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing files in AWS S3.
 */
@RestController
@RequestMapping("/api")
public class FileManagerController {

    @Autowired
    private S3FileService s3FileService;

    /**
     * Uploads an entire directory to the S3 bucket.
     *
     * @param localDir the path to the local directory to upload.
     * @return a map containing the uploaded file keys and their statuses.
     */
    @PostMapping("/upload")
    public Map<String, String> uploadDirectory(@RequestParam String localDir) {
        return s3FileService.uploadDirectory(localDir);
    }

    /**
     * Lists all files in the S3 bucket.
     *
     * @return a list of file keys in the S3 bucket.
     */
    @GetMapping("/files")
    public List<String> listFiles() {
        return s3FileService.listFiles();
    }

    /**
     * Retrieves the content of a specific file from the S3 bucket.
     *
     * @param key the key of the file to retrieve.
     * @return the content of the file as a string.
     */
    @GetMapping("/file")
    public String getFile(@RequestParam String key) {
        return s3FileService.getFileContent(key);
    }
}
