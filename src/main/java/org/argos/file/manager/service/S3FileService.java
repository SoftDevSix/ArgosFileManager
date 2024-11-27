package org.argos.file.manager.service;

import org.argos.file.manager.repository.S3Repository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * Service layer for AWS S3 file operations.
 */
@Service
public class S3FileService {

    private final S3Repository s3Repository;

    public S3FileService(S3Repository s3Repository) {
        this.s3Repository = s3Repository;
    }

    /**
     * Uploads a directory to the S3 bucket.
     *
     * @param localDir the path to the local directory.
     * @return a map of file keys and upload statuses.
     */
    public Map<String, String> uploadDirectory(String localDir) {
        return s3Repository.uploadDirectory(localDir);
    }

    /**
     * Lists all files in the S3 bucket.
     *
     * @return a list of file keys in the S3 bucket.
     */
    public List<String> listFiles() {
        return s3Repository.listFiles();
    }

    /**
     * Retrieves the content of a specific file from the S3 bucket.
     *
     * @param key the key of the file to retrieve.
     * @return the file content as a string.
     */
    public String getFileContent(String key) {
        return s3Repository.getFileContent(key);
    }
}
