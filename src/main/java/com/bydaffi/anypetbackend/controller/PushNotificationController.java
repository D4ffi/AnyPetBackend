package com.bydaffi.anypetbackend.controller;

import com.bydaffi.anypetbackend.dto.PushNotificationRequest;
import com.bydaffi.anypetbackend.service.PushNotificationService;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for sending push notifications to mobile devices.
 * Supports sending individual and batch notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;

    /**
     * Sends a push notification to a single device.
     *
     * Example requests:
     *
     * 1. Pet-specific notification:
     * {
     *   "petName": "Luna",
     *   "message": "tiene hambre",
     *   "token": "device-fcm-token",
     *   "title": "Alerta de Mascota"
     * }
     * Result: "Luna tiene hambre"
     *
     * 2. Another pet-specific notification:
     * {
     *   "petName": "Rocky",
     *   "message": "debería salir a pasear pronto",
     *   "token": "device-fcm-token"
     * }
     * Result: "Rocky debería salir a pasear pronto"
     *
     * 3. General notification (no pet name):
     * {
     *   "message": "Es hora de alimentar a tus mascotas",
     *   "token": "device-fcm-token"
     * }
     * Result: "Es hora de alimentar a tus mascotas"
     *
     * @param request The push notification request
     * @return ResponseEntity with success/failure message
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody PushNotificationRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate request
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "FCM token is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Message is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Send notification
            String messageId = pushNotificationService.sendPushNotification(request);

            response.put("success", true);
            response.put("message", "Notification sent successfully");
            response.put("messageId", messageId);

            log.info("Notification sent successfully. Message ID: {}", messageId);
            return ResponseEntity.ok(response);

        } catch (FirebaseMessagingException e) {
            log.error("Error sending push notification: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to send notification: " + e.getMessage());
            response.put("errorCode", e.getErrorCode());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            log.error("Unexpected error sending push notification: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Sends push notifications to multiple devices.
     *
     * Example request:
     * {
     *   "petName": "Luna",
     *   "message": "tiene hambre",
     *   "title": "Alerta de Mascota",
     *   "tokens": ["token1", "token2", "token3"]
     * }
     *
     * @param requestBody Map containing the notification request and list of tokens
     * @return ResponseEntity with batch send results
     */
    @PostMapping("/send/batch")
    public ResponseEntity<Map<String, Object>> sendBatchNotification(
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Extract tokens
            @SuppressWarnings("unchecked")
            List<String> tokens = (List<String>) requestBody.get("tokens");

            if (tokens == null || tokens.isEmpty()) {
                response.put("success", false);
                response.put("message", "At least one token is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Build request object
            PushNotificationRequest request = new PushNotificationRequest();
            request.setPetName((String) requestBody.get("petName"));
            request.setMessage((String) requestBody.get("message"));
            request.setTitle((String) requestBody.get("title"));

            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Message is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Send notifications
            BatchResponse batchResponse = pushNotificationService.sendPushNotificationToMultipleDevices(
                    request, tokens);

            response.put("success", true);
            response.put("message", "Batch notifications sent");
            response.put("successCount", batchResponse.getSuccessCount());
            response.put("failureCount", batchResponse.getFailureCount());
            response.put("totalCount", tokens.size());

            log.info("Batch notifications sent. Success: {}, Failure: {}",
                    batchResponse.getSuccessCount(), batchResponse.getFailureCount());

            return ResponseEntity.ok(response);

        } catch (FirebaseMessagingException e) {
            log.error("Error sending batch push notifications: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to send batch notifications: " + e.getMessage());
            response.put("errorCode", e.getErrorCode());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            log.error("Unexpected error sending batch push notifications: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Health check endpoint for notification service.
     *
     * @return Simple success message
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "Push Notification Service");
        return ResponseEntity.ok(response);
    }
}
