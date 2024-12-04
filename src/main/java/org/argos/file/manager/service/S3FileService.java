package org.argos.file.manager.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.argos.file.manager.repository.IStorageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service layer for AWS S3 file operations.
 * This service provides methods to upload a directory, list files in a project, and retrieve file content from an S3 bucket.
 */
@AllArgsConstructor
@Service
public class S3FileService {

    private final IStorageRepository storageRepository;

    /**
     * Uploads a directory to the S3 bucket and generates a new project ID.
     * The directory is uploaded to the S3 bucket under the generated project ID, and the status of each uploaded file is returned.
     *
     * @param localDir the path to the local directory to upload.
     * @return a map containing the generated project ID and the statuses of the uploaded files.
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
     * Uploads a ZIP file, extracts its contents, and stores them in the S3 bucket.
     *
     * @param file the ZIP file to be uploaded and processed.
     * @return a map containing the project ID and upload statuses.
     */
    public Map<String, Object> uploadZipFile(MultipartFile file) {
        String projectId = generateProjectId();
        Map<String, String> uploadResults =
                storageRepository.uploadMultiPartDirectory(projectId, file);

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
        return UUID.randomUUID().toString();
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
