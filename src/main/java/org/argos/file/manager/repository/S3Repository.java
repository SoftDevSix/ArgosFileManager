package org.argos.file.manager.repository;

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


@Repository
public class S3Repository implements IRepository {

    private final S3Client s3Client;
    private final String BUCKET_NAME = "group8-image-uploader-s3";

    public S3Repository(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    private boolean isBucketEmpty() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        return response.contents().isEmpty();
    }

    private void clearBucket() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        List<ObjectIdentifier> objectsToDelete = response.contents().stream()
                .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
                .collect(Collectors.toList());

        if (!objectsToDelete.isEmpty()) {
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(BUCKET_NAME)
                    .delete(Delete.builder().objects(objectsToDelete).build())
                    .build();
            s3Client.deleteObjects(deleteRequest);
        }
    }

    @Override
    public Map<String, String> uploadDirectory(String localDir) {
        if (!isBucketEmpty()) clearBucket();
        return uploadFiles(localDir);
    }

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
}
