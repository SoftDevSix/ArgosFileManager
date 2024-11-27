package org.argos.file.manager.service;

import org.argos.file.manager.repository.IStorageRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service layer for AWS S3 file operations.
 */
@Service
public class S3FileService {

    private final IStorageRepository storageRepository;

    public S3FileService(IStorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    /**
     * Uploads a directory to the S3 bucket and generates a new project ID.
     *
     * @param localDir the path to the local directory.
     * @return a map containing the generated project ID and uploaded file statuses.
     */
    public Map<String, Object> uploadDirectory(String localDir) {
        String projectId = generateProjectId();
        Map<String, String> uploadResults = storageRepository.uploadDirectory(projectId, localDir);

        Map<String, Object> response = new HashMap<>();
        response.put("projectId", projectId);
        response.put("uploadResults", uploadResults);

        return response;
    }

    /**
     * Generates a new unique project ID.
     *
     * @return a unique project ID as a string.
     */
    private String generateProjectId() {
        return UUID.randomUUID().toString();
    }

    public List<String> listFiles(String projectId) {
        return storageRepository.listFiles(projectId);
    }

    public String getFileContent(String projectId, String filePath) {
        return storageRepository.getFileContent(projectId, filePath);
    }
}
