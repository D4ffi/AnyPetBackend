package com.bydaffi.anypetbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for push notification requests.
 * Contains the message content and optional pet name for personalized notifications.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequest {

    /**
     * The name of the pet (optional).
     * Examples: "Luna", "Rocky"
     * If null, the notification is a general message.
     */
    private String petName;

    /**
     * The notification message to be sent.
     * Examples:
     * - "tiene hambre" (will be combined with petName: "Luna tiene hambre")
     * - "debería salir a pasear pronto" (will be combined with petName)
     * - "Es hora de alimentar a tus mascotas" (general message, no petName)
     */
    private String message;

    /**
     * FCM device token (required for sending to specific device)
     */
    private String token;

    /**
     * Optional title for the notification.
     * If not provided, a default title will be used.
     */
    private String title;
}
