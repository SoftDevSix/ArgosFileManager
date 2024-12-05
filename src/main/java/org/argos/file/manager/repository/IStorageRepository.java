package org.argos.file.manager.repository;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for storage repository operations.
 */
public interface IStorageRepository {

    /**
     * Uploads a local directory to the storage bucket for a specific project.
     *
     * @param projectId the ID of the project.
     * @param localDir the path to the local directory.
     * @return a map containing the uploaded file keys and their statuses.
     */
    Map<String, String> uploadDirectory(String projectId, String localDir);

    /**
     * Uploads a ZIP file to the storage bucket and unpacks it, preserving directory structure.
     *
     * @param projectId the ID of the project.
     * @param zipFilePath the path to the ZIP file.
     * @return a map containing the file keys and their upload statuses.
     */
    Map<String, String> uploadMultiPartDirectory(String projectId, MultipartFile zipFilePath);

    /**
     * Lists all files in the storage bucket for a specific project.
     *
     * @param projectId the ID of the project.
     * @return a list of file keys.
     */
    List<String> listFiles(String projectId);

    /**
     * Retrieves the content of a specific file.
     *
     * @param projectId the ID of the project.
     * @param filePath the relative path of the file.
     * @return the content of the file as a string.
     */
    String getFileContent(String projectId, String filePath);
}
