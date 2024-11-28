package org.argos.file.manager.repository;

import java.nio.file.*;
import java.util.*;
import org.argos.file.manager.exceptions.BadRequestError;
import org.argos.file.manager.exceptions.NotFoundError;
import org.argos.file.manager.utils.FileProcessor;
import org.argos.file.manager.utils.InputValidator;
import org.argos.file.manager.utils.S3KeyGenerator;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

/**
 * Repository implementation for interacting with AWS S3.
 * Provides methods to upload files, list project files, and retrieve file content.
 */
@Repository
public class S3Repository implements IStorageRepository {

    private final S3Client s3Client;
    private final String bucketName;

    /**
     * Constructs a new S3Repository with the given S3 client.
     * The bucket name is loaded from environment variables using Dotenv.
     *
     * @param s3Client the S3 client to use for interacting with the S3 bucket.
     */
    public S3Repository(S3Client s3Client) {
        this.s3Client = s3Client;
        this.bucketName = System.getenv("AWS_BUCKET_NAME");
    }

    /**
     * Uploads all files from a local directory to the S3 bucket under a specific project.
     *
     * @param projectId the unique identifier for the project.
     * @param localDir the local directory path containing the files to be uploaded.
     * @return a map containing the file paths as keys and upload status as values.
     */
    @Override
    public Map<String, String> uploadDirectory(String projectId, String localDir) {
        InputValidator.getInstance().validateProjectId(projectId);
        Path directory = InputValidator.getInstance().validateDirectory(localDir);

        List<Path> files = FileProcessor.getInstance().getFilesFromDirectory(directory);
        FileProcessor.getInstance().validateFilesExist(files);

        Map<String, String> result = new HashMap<>();
        uploadFiles(projectId, directory, files, result);
        return result;
    }

    /**
     * Uploads multiple files to S3 under the specified project.
     *
     * @param projectId the unique identifier for the project.
     * @param directory the root directory of the files being uploaded.
     * @param files the list of files to upload.
     * @param result a map to store upload results.
     */
    private void uploadFiles(String projectId, Path directory, List<Path> files, Map<String, String> result) {
        for (Path file : files) {
            uploadSingleFile(projectId, directory, file, result);
        }
    }

    /**
     * Uploads a single file to S3.
     *
     * @param projectId the unique identifier for the project.
     * @param directory the root directory of the files being uploaded.
     * @param file the file to upload.
     * @param result a map to store upload results.
     */
    private void uploadSingleFile(String projectId, Path directory, Path file, Map<String, String> result) {
        String key = S3KeyGenerator.generateKey(projectId, directory, file);
        try {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucketName).key(key).build(),
                    RequestBody.fromFile(file)
            );
            result.put(key, "Uploaded");
        } catch (S3Exception e) {
            throw new BadRequestError("Failed to upload files to S3: " + e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * Lists all the files stored in the S3 bucket for a specific project.
     *
     * @param projectId the unique identifier for the project.
     * @return a list of file paths (keys) for the project stored in the S3 bucket.
     */
    @Override
    public List<String> listFiles(String projectId) {
        InputValidator.getInstance().validateProjectId(projectId);
        String prefix = String.format("projects/%s/", projectId);

        try {
            ListObjectsV2Request request =
                    ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).build();
            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            if (response.contents().isEmpty()) {
                throw new NotFoundError("No files found for project ID: " + projectId);
            }

            return response.contents().stream().map(S3Object::key).toList();
        } catch (S3Exception e) {
            String errorMessage = e.awsErrorDetails() != null
                    ? e.awsErrorDetails().errorMessage()
                    : "Error occurred";

            throw new BadRequestError("Failed to list files: " + errorMessage);
        }
    }

    /**
     * Retrieves the content of a specific file stored in the S3 bucket for a given project.
     *
     * @param projectId the unique identifier for the project.
     * @param filePath the path of the file within the S3 bucket.
     * @return the content of the file as a String.
     */
    @Override
    public String getFileContent(String projectId, String filePath) {
        InputValidator.getInstance().validateProjectId(projectId);
        InputValidator.getInstance().validateFilePath(filePath);

        String key = String.format("projects/%s/%s", projectId, filePath);

        try {
            GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(key).build();
            return s3Client.getObjectAsBytes(request).asUtf8String();
        } catch (NoSuchKeyException e) {
            throw new NotFoundError("File not found: " + filePath);
        } catch (S3Exception e) {
            throw new BadRequestError("Failed to retrieve file: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            throw new BadRequestError("Error reading file content: " + e.getMessage());
        }
    }
}
