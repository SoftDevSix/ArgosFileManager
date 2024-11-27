package org.argos.file.manager.repository;

import java.util.List;
import java.util.Map;

/**
 * Interface for storage repository operations.
 */
public interface IStorageRepository {

    /**
     * Uploads a local directory to the storage bucket.
     *
     * @param localDir the path to the local directory.
     * @return a map containing the uploaded file keys and their statuses.
     */
    Map<String, String> uploadDirectory(String localDir);

    /**
     * Lists all files in the storage bucket.
     *
     * @return a list of file keys.
     */
    List<String> listFiles();

    /**
     * Retrieves the content of a specific file.
     *
     * @param key the key of the file to retrieve.
     * @return the content of the file as a string.
     */
    String getFileContent(String key);
}
