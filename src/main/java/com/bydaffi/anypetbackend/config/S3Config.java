package com.bydaffi.anypetbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuration class for AWS S3 client using IAM Roles.
 *
 * This configuration uses DefaultCredentialsProvider which automatically searches for credentials in:
 * 1. Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
 * 2. System properties
 * 3. AWS credentials file (~/.aws/credentials)
 * 4. IAM instance profile credentials (recommended for EC2/Elastic Beanstalk)
 *
 * For AWS Elastic Beanstalk deployment:
 * - Attach an IAM Role to your Beanstalk environment with S3 permissions
 * - No need to manage access keys or secret keys
 * - Credentials are automatically rotated and managed by AWS
 *
 * Required properties in application.properties:
 * - aws.s3.region (e.g., us-east-1)
 * - aws.s3.bucket-name (e.g., anypet-images-bucket)
 */
@Configuration
public class S3Config {

    @Value("${aws.s3.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // Use DefaultCredentialsProvider for automatic credential discovery
        // In production (Beanstalk), this will use the IAM Role attached to the EC2 instance
        // In development, it will use local AWS credentials or environment variables
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
