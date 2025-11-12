package com.bydaffi.anypetbackend.service;

import com.bydaffi.anypetbackend.dto.PushNotificationRequest;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending push notifications using Firebase Cloud Messaging (FCM).
 */
@Service
@Slf4j
public class PushNotificationService {

    /**
     * Sends a push notification to a specific device.
     *
     * @param request The push notification request containing message, token, and optional pet name
     * @return The message ID if successful
     * @throws FirebaseMessagingException if sending fails
     */
    public String sendPushNotification(PushNotificationRequest request) throws FirebaseMessagingException {
        // Build the notification message
        String fullMessage = buildMessage(request.getPetName(), request.getMessage());
        String title = request.getTitle() != null ? request.getTitle() : "AnyPet";

        // Create notification
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(fullMessage)
                .build();

        // Add custom data payload
        Map<String, String> data = new HashMap<>();
        if (request.getPetName() != null) {
            data.put("petName", request.getPetName());
        }
        data.put("message", request.getMessage());
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        // Build the message
        Message message = Message.builder()
                .setToken(request.getToken())
                .setNotification(notification)
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setSound("default")
                                .setColor("#FF6B6B")
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .setBadge(1)
                                .build())
                        .build())
                .build();

        // Send the message
        String response = FirebaseMessaging.getInstance().send(message);
        log.info("Successfully sent push notification. Message ID: {}", response);

        return response;
    }

    /**
     * Sends push notifications to multiple devices.
     *
     * @param request The push notification request
     * @param tokens List of FCM device tokens
     * @return BatchResponse containing results for each token
     * @throws FirebaseMessagingException if sending fails
     */
    public BatchResponse sendPushNotificationToMultipleDevices(
            PushNotificationRequest request,
            java.util.List<String> tokens) throws FirebaseMessagingException {

        String fullMessage = buildMessage(request.getPetName(), request.getMessage());
        String title = request.getTitle() != null ? request.getTitle() : "AnyPet";

        // Create notification
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(fullMessage)
                .build();

        // Add custom data payload
        Map<String, String> data = new HashMap<>();
        if (request.getPetName() != null) {
            data.put("petName", request.getPetName());
        }
        data.put("message", request.getMessage());
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        // Build multicast message
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(notification)
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setSound("default")
                                .setColor("#FF6B6B")
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .setBadge(1)
                                .build())
                        .build())
                .build();

        // Send the message to multiple devices
        BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
        log.info("Successfully sent {} notifications. Failure count: {}",
                response.getSuccessCount(), response.getFailureCount());

        return response;
    }

    /**
     * Builds the complete message by combining pet name and message.
     *
     * @param petName Optional pet name
     * @param message The message content
     * @return The complete formatted message
     */
    private String buildMessage(String petName, String message) {
        if (petName != null && !petName.trim().isEmpty()) {
            return petName + " " + message;
        }
        return message;
    }
}
