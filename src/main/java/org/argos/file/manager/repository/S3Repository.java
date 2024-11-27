package org.argos.file.manager.repository;

import io.github.cdimascio.dotenv.Dotenv;
import org.argos.file.manager.exceptions.BadRequestError;
import org.argos.file.manager.exceptions.InternalServerError;
import org.argos.file.manager.exceptions.NotFoundError;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

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

        Dotenv dotenv = Dotenv.load();
        this.bucketName = dotenv.get("AWS_BUCKET_NAME");
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
        if (projectId == null || projectId.isBlank()) {
            throw new BadRequestError("Project ID cannot be null or empty.");
        }

        Path directory = Paths.get(localDir);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new BadRequestError("Invalid local directory: " + localDir);
        }

        Map<String, String> result = new HashMap<>();
        try (Stream<Path> stream = Files.walk(directory)) {
            List<Path> files = stream.filter(Files::isRegularFile).toList();

            if (files.isEmpty()) {
                throw new BadRequestError("No files found in the directory to upload.");
            }

            for (Path file : files) {
                String key = String.format("projects/%s/%s",
                        projectId,
                        directory.relativize(file).toString().replace("\\", "/"));

                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build(),
                        RequestBody.fromFile(file)
                );
                result.put(key, "Uploaded");
            }
        } catch (IOException e) {
            throw new NotFoundError("Failed to read files from directory: " + e.getMessage());
        } catch (S3Exception e) {
            throw new BadRequestError("Failed to upload files to S3: " + e.awsErrorDetails().errorMessage());
        }
        return result;
    }

    /**
     * Lists all the files stored in the S3 bucket for a specific project.
     *
     * @param projectId the unique identifier for the project.
     * @return a list of file paths (keys) for the project stored in the S3 bucket.
     */
    @Override
    public List<String> listFiles(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new BadRequestError("Project ID cannot be null or empty.");
        }

        String prefix = String.format("projects/%s/", projectId);

        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            if (response.contents().isEmpty()) {
                throw new NotFoundError("No files found for project ID: " + projectId);
            }

            return response.contents()
                    .stream()
                    .map(S3Object::key)
                    .toList();
        } catch (S3Exception e) {
            String errorMessage = e.awsErrorDetails() != null
                    ? e.awsErrorDetails().errorMessage()
                    : "Unknown error occurred";

            throw new InternalServerError("Failed to list files: " + errorMessage);
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
        if (projectId == null || projectId.isBlank() || filePath == null || filePath.isBlank()) {
            throw new BadRequestError("Project ID and file path cannot be null or empty.");
        }

        String key = String.format("projects/%s/%s", projectId, filePath);

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            try (InputStream inputStream = s3Client.getObject(request)) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (NoSuchKeyException e) {
            throw new NotFoundError("File not found: " + filePath);
        } catch (S3Exception e) {
            throw new BadRequestError("Failed to retrieve file: " + e.awsErrorDetails().errorMessage());
        } catch (IOException e) {
            throw new BadRequestError("Error reading file content: " + e.getMessage());
        }
    }
}
