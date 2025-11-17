package com.bydaffi.anypetbackend.dto;

import com.bydaffi.anypetbackend.models.Reminder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating and updating reminders.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReminderRequest {

    /**
     * Title of the reminder (e.g., "Alimentar a Luna", "Paseo de Rocky")
     */
    private String title;

    /**
     * Optional detailed message
     */
    private String message;

    /**
     * Time of day in HH:mm format (e.g., "09:00", "18:30")
     */
    private String scheduledTime;

    /**
     * Repeat interval: ONCE, DAILY, WEEKLY, MONTHLY, YEARLY,
     * EVERY_HOUR, EVERY_2_HOURS, EVERY_4_HOURS, EVERY_6_HOURS, EVERY_12_HOURS
     */
    private String repeatInterval;

    /**
     * Firebase UID of the user
     */
    private String userId;

    /**
     * Optional: Pet ID associated with this reminder
     */
    private Long petId;

    /**
     * FCM device token for sending notifications
     */
    private String deviceToken;

    /**
     * Whether the reminder is active
     */
    private Boolean active = true;
}
