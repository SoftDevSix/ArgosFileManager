package org.argos.file.manager.repository;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Repository implementation for interacting with AWS S3.
 * Provides methods for uploading directories, listing files, and retrieving file contents.
 */
@Repository
public class S3Repository implements IStorageRepository {

    private final S3Client s3Client;
    private final String BUCKET_NAME;

    /**
     * Constructs a new S3Repository with the given S3 client.
     * The bucket name is loaded from environment variables.
     *
     * @param s3Client the S3 client to use for interacting with the S3 bucket.
     */
    public S3Repository(S3Client s3Client) {
        this.s3Client = s3Client;

        Dotenv dotenv = Dotenv.load();
        this.BUCKET_NAME = dotenv.get("AWS_BUCKET_NAME");
    }

    /**
     * Uploads all files in the specified local directory to the S3 bucket.
     * If the bucket is not empty, it clears the bucket before uploading.
     *
     * @param localDir the local directory to upload.
     * @return a map where the keys are the file paths (relative to the directory) and the values are the upload statuses.
     */
    @Override
    public Map<String, String> uploadDirectory(String localDir) {
        if (!isBucketEmpty()) clearBucket();
        return uploadFiles(localDir);
    }

    /**
     * Lists all files currently stored in the S3 bucket.
     *
     * @return a list of file keys (paths) in the S3 bucket.
     */
    @Override
    public List<String> listFiles() {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(BUCKET_NAME)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            return response.contents()
                    .stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of("Error fetching file list: " + e.getMessage());
        }
    }

    /**
     * Retrieves the content of a file from the S3 bucket by its key.
     *
     * @param key the key of the file to retrieve.
     * @return the content of the file as a string, or an error message if the retrieval fails.
     */
    @Override
    public String getFileContent(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();

            try (InputStream inputStream = s3Client.getObject(request)) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to retrieve file: " + e.getMessage();
        }
    }

    /**
     * Checks if the S3 bucket is empty.
     *
     * @return {@code true} if the bucket is empty, {@code false} otherwise.
     */
    private boolean isBucketEmpty() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        return response.contents().isEmpty();
    }

    /**
     * Clears all files in the S3 bucket by deleting them.
     *
     * @throws RuntimeException if an error occurs while clearing the bucket.
     */
    private void clearBucket() {
        try {
            List<ObjectIdentifier> objectsToDelete = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                            .bucket(BUCKET_NAME)
                            .build())
                    .contents()
                    .stream()
                    .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
                    .collect(Collectors.toList());

            if (!objectsToDelete.isEmpty()) {
                s3Client.deleteObjects(DeleteObjectsRequest.builder()
                        .bucket(BUCKET_NAME)
                        .delete(Delete.builder().objects(objectsToDelete).build())
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear bucket: " + e.getMessage(), e);
        }
    }

    /**
     * Uploads all files in the specified local directory to the S3 bucket.
     *
     * @param localDir the local directory to upload.
     * @return a map where the keys are the file paths (relative to the directory) and the values are the upload statuses.
     */
    private Map<String, String> uploadFiles(String localDir) {
        Map<String, String> result = new HashMap<>();
        Path directory = Paths.get(localDir);

        try {
            List<Path> files = Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            for (Path file : files) {
                String key = directory.relativize(file).toString().replace("\\", "/");

                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(BUCKET_NAME)
                                .key(key)
                                .build(),
                        RequestBody.fromFile(file)
                );
                result.put(key, "Uploaded");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", "Failed to upload files: " + e.getMessage());
        }

        return result;
    }
}
