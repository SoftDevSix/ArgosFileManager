package org.argos.file.manager.service;

import org.argos.file.manager.repository.S3Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class S3FileService {

    private final S3Repository s3Repository;

    public S3FileService(S3Repository s3Repository) {
        this.s3Repository = s3Repository;
    }

    public Map<String, String> uploadDirectory(String localDir) {
        return s3Repository.uploadDirectory(localDir);
    }

    public List<String> listFiles() {
        return s3Repository.listFiles();
    }

    public String getFileContent(String key) {
        return s3Repository.getFileContent(key);
    }
}
