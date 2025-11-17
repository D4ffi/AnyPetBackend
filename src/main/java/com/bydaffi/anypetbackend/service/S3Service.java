package com.bydaffi.anypetbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for managing file uploads and downloads to/from AWS S3.
 * Handles pet profile images and vaccine batch lot images.
 */
@Service
public class S3Service {

    private final S3Client s3Client;
    private final ImageCompressionService imageCompressionService;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${app.image.max-size-mb:10}")
    private int maxSizeMB;

    public S3Service(S3Client s3Client, ImageCompressionService imageCompressionService) {
        this.s3Client = s3Client;
        this.imageCompressionService = imageCompressionService;
    }

    /**
     * Uploads a pet profile image to S3
     *
     * @param file the image file
     * @param petId the pet ID
     * @return the S3 URL of the uploaded image
     * @throws IOException if upload fails
     */
    public String uploadPetProfileImage(MultipartFile file, Long petId) throws IOException {
        validateImage(file);

        // Compress the image
        byte[] compressedImage = imageCompressionService.compressImage(file);

        // Generate unique filename
        String filename = generateFilename("pets/profiles", petId.toString(), file.getOriginalFilename());

        // Upload to S3
        return uploadToS3(compressedImage, filename, file.getContentType());
    }

    /**
     * Uploads a vaccine batch lot image to S3
     *
     * @param file the image file
     * @param vaccinationRecordId the vaccination record ID
     * @return the S3 URL of the uploaded image
     * @throws IOException if upload fails
     */
    public String uploadVaccineBatchImage(MultipartFile file, Long vaccinationRecordId) throws IOException {
        validateImage(file);

        // Compress the image
        byte[] compressedImage = imageCompressionService.compressImage(file);

        // Generate unique filename
        String filename = generateFilename("vaccines/batches", vaccinationRecordId.toString(), file.getOriginalFilename());

        // Upload to S3
        return uploadToS3(compressedImage, filename, file.getContentType());
    }

    /**
     * Uploads a thumbnail version of an image
     *
     * @param file the original image file
     * @param type the type of image (pet or vaccine)
     * @param entityId the entity ID
     * @return the S3 URL of the uploaded thumbnail
     * @throws IOException if upload fails
     */
    public String uploadThumbnail(MultipartFile file, String type, Long entityId) throws IOException {
        validateImage(file);

        // Create thumbnail
        byte[] thumbnail = imageCompressionService.createThumbnail(file);

        // Generate unique filename for thumbnail
        String prefix = type.equals("pet") ? "pets/thumbnails" : "vaccines/thumbnails";
        String filename = generateFilename(prefix, entityId.toString(), file.getOriginalFilename());

        // Upload to S3
        return uploadToS3(thumbnail, filename, file.getContentType());
    }

    /**
     * Deletes an image from S3
     *
     * @param imageUrl the full S3 URL of the image
     */
    public void deleteImage(String imageUrl) {
        try {
            // Extract key from URL
            String key = extractKeyFromUrl(imageUrl);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image from S3: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a presigned URL for temporary direct access to an image
     *
     * @param imageUrl the S3 URL
     * @return presigned URL (valid for 1 hour)
     */
    public String getPresignedUrl(String imageUrl) {
        // For production, implement presigned URL generation
        // For now, return the direct URL (make sure bucket has proper CORS)
        return imageUrl;
    }

    /**
     * Checks if an image exists in S3
     *
     * @param imageUrl the S3 URL
     * @return true if exists
     */
    public boolean imageExists(String imageUrl) {
        try {
            String key = extractKeyFromUrl(imageUrl);

            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    /**
     * Private helper: Upload bytes to S3
     */
    private String uploadToS3(byte[] imageBytes, String key, String contentType) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) imageBytes.length)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes));

            // Return the S3 URL
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Private helper: Generate unique filename
     */
    private String generateFilename(String prefix, String entityId, String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);

        return String.format("%s/%s_%s_%s.%s", prefix, entityId, timestamp, uuid, extension);
    }

    /**
     * Private helper: Extract file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Private helper: Extract S3 key from full URL
     */
    private String extractKeyFromUrl(String url) {
        // URL format: https://bucket-name.s3.amazonaws.com/key
        // Or: https://s3.amazonaws.com/bucket-name/key
        String[] parts = url.split(bucketName + "/");
        if (parts.length > 1) {
            return parts[1];
        }

        // Fallback: extract everything after .com/
        parts = url.split("\\.com/");
        if (parts.length > 1) {
            return parts[1];
        }

        throw new IllegalArgumentException("Invalid S3 URL format: " + url);
    }

    /**
     * Private helper: Validate image file
     */
    private void validateImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (!imageCompressionService.isValidImage(file)) {
            throw new IOException("File is not a valid image");
        }

        // Check file size (before compression)
        double fileSizeMB = file.getSize() / (1024.0 * 1024.0);
        if (fileSizeMB > maxSizeMB) {
            throw new IOException(String.format("File size (%.2f MB) exceeds maximum allowed size (%d MB)",
                    fileSizeMB, maxSizeMB));
        }
    }

    /**
     * Get image metadata
     */
    public ImageMetadata getImageMetadata(String imageUrl) {
        try {
            String key = extractKeyFromUrl(imageUrl);

            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headRequest);

            return new ImageMetadata(
                    key,
                    response.contentLength(),
                    response.contentType(),
                    response.lastModified().toString()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to get image metadata: " + e.getMessage(), e);
        }
    }

    /**
     * Inner class for image metadata
     */
    public static class ImageMetadata {
        private final String key;
        private final Long size;
        private final String contentType;
        private final String lastModified;

        public ImageMetadata(String key, Long size, String contentType, String lastModified) {
            this.key = key;
            this.size = size;
            this.contentType = contentType;
            this.lastModified = lastModified;
        }

        public String getKey() { return key; }
        public Long getSize() { return size; }
        public String getContentType() { return contentType; }
        public String getLastModified() { return lastModified; }
    }
}
