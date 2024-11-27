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
import java.util.stream.Collectors;

/**
 * Repository implementation for interacting with AWS S3.
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

    @Override
    public Map<String, String> uploadDirectory(String projectId, String localDir) {
        Map<String, String> result = new HashMap<>();
        Path directory = Paths.get(localDir);

        try {
            List<Path> files = Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            for (Path file : files) {
                String key = String.format("projects/%s/%s", projectId, directory.relativize(file).toString().replace("\\", "/"));
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

    @Override
    public List<String> listFiles(String projectId) {
        String prefix = String.format("projects/%s/", projectId);

        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(BUCKET_NAME)
                    .prefix(prefix)
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

    @Override
    public String getFileContent(String projectId, String filePath) {
        String key = String.format("projects/%s/%s", projectId, filePath);

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
}
