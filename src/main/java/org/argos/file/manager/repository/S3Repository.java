package org.argos.file.manager.repository;

import io.github.cdimascio.dotenv.Dotenv;
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
 * This class provides methods to upload a directory of files, list files in a project, and retrieve the content of a specific file from an S3 bucket.
 */
@Repository
public class S3Repository implements IStorageRepository {

    private final S3Client s3Client;
    private final String bucketName;

    /**
     * Constructs a new S3Repository with the given S3 client.
     * The bucket name is loaded from environment variables using the Dotenv library.
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
     * The local directory structure will be preserved within the S3 bucket.
     *
     * @param projectId the unique identifier for the project.
     * @param localDir the local directory path containing the files to be uploaded.
     * @return a map containing the file paths as keys and upload status as values.
     */
    @Override
    public Map<String, String> uploadDirectory(String projectId, String localDir) {
        Map<String, String> result = new HashMap<>();
        Path directory = Paths.get(localDir);

        try (Stream<Path> stream = Files.walk(directory)) {
            List<Path> files = stream.filter(Files::isRegularFile).toList();

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
        } catch (Exception e) {
            result.put("error", "Failed to upload files: " + e.getMessage());
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
        String prefix = String.format("projects/%s/", projectId);
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            return response.contents()
                    .stream()
                    .map(S3Object::key)
                    .toList();
        } catch (Exception e) {
            return List.of("Error fetching file list: " + e.getMessage());
        }
    }


    /**
     * Retrieves the content of a specific file stored in the S3 bucket for a given project.
     *
     * @param projectId the unique identifier for the project.
     * @param filePath the path of the file within the S3 bucket.
     * @return the content of the file as a String, or an error message if the file could not be retrieved.
     */
    @Override
    public String getFileContent(String projectId, String filePath) {
        String key = String.format("projects/%s/%s", projectId, filePath);

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            try (InputStream inputStream = s3Client.getObject(request)) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            return "Failed to retrieve file: " + e.getMessage();
        }
    }
}
