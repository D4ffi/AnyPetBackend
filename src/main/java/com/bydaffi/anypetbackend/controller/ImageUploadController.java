package com.bydaffi.anypetbackend.controller;

import com.bydaffi.anypetbackend.service.S3Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for handling image uploads for pets and vaccine batches.
 * Supports profile images for pets and batch lot images for vaccines.
 */
@RestController
@RequestMapping("/api/images")
public class ImageUploadController {

    private final S3Service s3Service;
    private final FirebaseAuth firebaseAuth;

    public ImageUploadController(S3Service s3Service, FirebaseAuth firebaseAuth) {
        this.s3Service = s3Service;
        this.firebaseAuth = firebaseAuth;
    }

    /**
     * Helper method to verify Firebase token and extract user ID
     */
    private String verifyTokenAndGetUserId(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
        return decodedToken.getUid();
    }

    /**
     * POST /api/images/pet/{petId}/profile
     * Upload a profile image for a pet
     *
     * Requires Firebase Authentication token in Authorization header
     *
     * @param petId the pet ID
     * @param file the image file
     * @param authorization Firebase auth token (Bearer token)
     * @return response with image URL
     */
    @PostMapping("/pet/{petId}/profile")
    public ResponseEntity<ImageUploadResponse> uploadPetProfileImage(
            @PathVariable Long petId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authorization) {

        try {
            // Verify Firebase token and get user ID
            String userId = verifyTokenAndGetUserId(authorization);

            // Upload image with user ID for proper S3 organization
            String imageUrl = s3Service.uploadPetProfileImage(file, petId, userId);
            String thumbnailUrl = s3Service.uploadThumbnail(file, "pet", petId, userId);

            ImageUploadResponse response = new ImageUploadResponse(
                    true,
                    "Pet profile image uploaded successfully",
                    imageUrl,
                    thumbnailUrl,
                    petId,
                    "PET_PROFILE"
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            ImageUploadResponse errorResponse = new ImageUploadResponse(
                    false,
                    "Authentication failed: " + e.getMessage(),
                    null,
                    null,
                    petId,
                    "PET_PROFILE"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            ImageUploadResponse errorResponse = new ImageUploadResponse(
                    false,
                    "Failed to upload image: " + e.getMessage(),
                    null,
                    null,
                    petId,
                    "PET_PROFILE"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * POST /api/images/vaccine/{vaccinationRecordId}/batch
     * Upload a batch lot image for a vaccination record
     *
     * Requires Firebase Authentication token in Authorization header
     *
     * @param vaccinationRecordId the vaccination record ID
     * @param file the image file
     * @param authorization Firebase auth token (Bearer token)
     * @return response with image URL
     */
    @PostMapping("/vaccine/{vaccinationRecordId}/batch")
    public ResponseEntity<ImageUploadResponse> uploadVaccineBatchImage(
            @PathVariable Long vaccinationRecordId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authorization) {

        try {
            // Verify Firebase token and get user ID
            String userId = verifyTokenAndGetUserId(authorization);

            // Upload image with user ID for proper S3 organization
            String imageUrl = s3Service.uploadVaccineBatchImage(file, vaccinationRecordId, userId);
            String thumbnailUrl = s3Service.uploadThumbnail(file, "vaccine", vaccinationRecordId, userId);

            ImageUploadResponse response = new ImageUploadResponse(
                    true,
                    "Vaccine batch image uploaded successfully",
                    imageUrl,
                    thumbnailUrl,
                    vaccinationRecordId,
                    "VACCINE_BATCH"
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            ImageUploadResponse errorResponse = new ImageUploadResponse(
                    false,
                    "Authentication failed: " + e.getMessage(),
                    null,
                    null,
                    vaccinationRecordId,
                    "VACCINE_BATCH"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            ImageUploadResponse errorResponse = new ImageUploadResponse(
                    false,
                    "Failed to upload image: " + e.getMessage(),
                    null,
                    null,
                    vaccinationRecordId,
                    "VACCINE_BATCH"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * DELETE /api/images
     * Delete an image from S3
     *
     * @param imageUrl the S3 URL of the image to delete
     * @return success response
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteImage(@RequestParam("url") String imageUrl) {
        try {
            s3Service.deleteImage(imageUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image deleted successfully");
            response.put("deletedUrl", imageUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete image: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/images/metadata
     * Get metadata for an image
     *
     * @param imageUrl the S3 URL
     * @return image metadata
     */
    @GetMapping("/metadata")
    public ResponseEntity<S3Service.ImageMetadata> getImageMetadata(@RequestParam("url") String imageUrl) {
        try {
            S3Service.ImageMetadata metadata = s3Service.getImageMetadata(imageUrl);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * GET /api/images/exists
     * Check if an image exists in S3
     *
     * @param imageUrl the S3 URL
     * @return existence status
     */
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> checkImageExists(@RequestParam("url") String imageUrl) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", s3Service.imageExists(imageUrl));
        return ResponseEntity.ok(response);
    }

    /**
     * Response DTO for image uploads
     */
    public static class ImageUploadResponse {
        private final boolean success;
        private final String message;
        private final String imageUrl;
        private final String thumbnailUrl;
        private final Long entityId;
        private final String imageType;

        public ImageUploadResponse(boolean success, String message, String imageUrl,
                                   String thumbnailUrl, Long entityId, String imageType) {
            this.success = success;
            this.message = message;
            this.imageUrl = imageUrl;
            this.thumbnailUrl = thumbnailUrl;
            this.entityId = entityId;
            this.imageType = imageType;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getImageUrl() { return imageUrl; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public Long getEntityId() { return entityId; }
        public String getImageType() { return imageType; }
    }
}
