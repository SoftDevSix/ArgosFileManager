package org.argos.file.manager.repository;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;


@Repository
public class S3Repository implements IRepository {

    private final S3Client s3Client;
    private final String BUCKET_NAME = "group8-image-uploader-s3";

    public S3Repository(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public Map<String, String> uploadDirectory(String localDir) {
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
            return Collections.singletonList("Error fetching file list: " + e.getMessage());
        }
    }

    @Override
    public String getFileContent(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();

            try (InputStream inputStream = s3Client.getObject(getObjectRequest)) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to retrieve file: " + e.getMessage();
        }
    }
}
