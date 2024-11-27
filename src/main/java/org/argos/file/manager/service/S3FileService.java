package org.argos.file.manager.service;

import org.argos.file.manager.repository.IStorageRepository;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Service layer for AWS S3 file operations.
 * This service provides methods to upload a directory, list files in a project, and retrieve file content from an S3 bucket.
 */
@Service
public class S3FileService {

    private final IStorageRepository storageRepository;

    /**
     * Constructs a new S3FileService with the given storage repository.
     *
     * @param storageRepository the repository for interacting with the S3 storage.
     */
    public S3FileService(IStorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    /**
     * Uploads a directory to the S3 bucket and generates a new project ID.
     * The directory is uploaded to the S3 bucket under the generated project ID, and the status of each uploaded file is returned.
     *
     * @param localDir the path to the local directory to upload.
     * @return a map containing the generated project ID and the statuses of the uploaded files.
     */
    public Map<String, Object> uploadDirectory(String localDir) {
        String projectId = generateProjectId();  // Generate a unique project ID
        Map<String, String> uploadResults = storageRepository.uploadDirectory(projectId, localDir);

        Map<String, Object> response = new HashMap<>();
        response.put("projectId", projectId);
        response.put("uploadResults", uploadResults);

        return response;
    }

    /**
     * Generates a new unique project ID.
     * This project ID is used to organize and identify the files uploaded to S3.
     *
     * @return a unique project ID as a string.
     */
    private String generateProjectId() {
        return UUID.randomUUID().toString();  // Generate a UUID as the project ID
    }

    /**
     * Lists all files stored in the S3 bucket under the specified project ID.
     *
     * @param projectId the unique identifier for the project.
     * @return a list of file paths (keys) for the project stored in the S3 bucket.
     */
    public List<String> listFiles(String projectId) {
        return storageRepository.listFiles(projectId);
    }

    /**
     * Retrieves the content of a specific file stored in the S3 bucket for a given project.
     *
     * @param projectId the unique identifier for the project.
     * @param filePath the path of the file within the S3 bucket.
     * @return the content of the file as a String.
     */
    public String getFileContent(String projectId, String filePath) {
        return storageRepository.getFileContent(projectId, filePath);
    }
}
