package org.argos.file.manager.controller;

import org.argos.file.manager.service.S3FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class FileManagerController {

    @Autowired
    private S3FileService s3FileService;

    /**
     * POST /upload - Upload an entire directory to S3
     */
    @PostMapping("/upload")
    public Map<String, String> uploadDirectory(@RequestParam String localDir) {
        return s3FileService.uploadDirectory(localDir);
    }

    /**
     * GET /files - List all files in the S3 bucket
     */
    @GetMapping("/files")
    public List<String> listFiles() {
        return s3FileService.listFiles();
    }

    /**
     * GET /file - Get content of a specific file by key
     */
    @GetMapping("/file")
    public String getFile(@RequestParam String key) {
        return s3FileService.getFileContent(key);
    }
}
