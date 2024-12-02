package org.argos.file.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuration class for setting up the AWS S3 client.
 */
@Configuration
public class S3Config {

    /**
     * Creates and configures an S3 client bean.
     *
     * @return an S3Client instance configured with credentials and region from environment variables.
     */
    @Bean
    public S3Client s3Client() {
        String accessKeyId = System.getenv("AWS_ACCESS_KEY_ID");
        String secretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        String region = System.getenv("AWS_REGION");

        if (accessKeyId == null || secretAccessKey == null || region == null) {
            throw new IllegalStateException("AWS environment variables are not set.");
        }

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();
    }
}
