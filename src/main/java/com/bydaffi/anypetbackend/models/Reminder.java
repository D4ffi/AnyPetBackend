package com.bydaffi.anypetbackend.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * POJO representing a reminder for pet-related activities.
 * Reminders are stored only in Firebase Firestore.
 * Reminders can be scheduled at specific times and can repeat at defined intervals.
 */
@Getter
@Setter
@NoArgsConstructor
public class Reminder {

    /**
     * Firebase document ID (primary identifier)
     */
    private String id;

    /**
     * Title of the reminder (e.g., "Alimentar a Luna", "Pasear a Rocky")
     */
    private String title;

    /**
     * Detailed description or message for the reminder
     */
    private String message;

    /**
     * Time of day when the reminder should trigger (HH:mm format)
     */
    private LocalTime scheduledTime;

    /**
     * Interval for repeating the reminder
     */
    private RepeatInterval repeatInterval;

    /**
     * User ID who owns this reminder (Firebase UID)
     */
    private String userId;

    /**
     * Optional: associated pet ID
     */
    private Long petId;

    /**
     * FCM device token for sending push notifications
     */
    private String deviceToken;

    /**
     * Whether the reminder is currently active
     */
    private boolean active = true;

    /**
     * Last time this reminder was triggered
     */
    private LocalDateTime lastTriggered;

    /**
     * Next scheduled execution time (calculated based on scheduledTime and repeatInterval)
     */
    private LocalDateTime nextExecution;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Initializes timestamps when creating a new reminder
     */
    public void initializeTimestamps() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        if (nextExecution == null) {
            calculateNextExecution();
        }
    }

    /**
     * Updates the updatedAt timestamp
     */
    public void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculates the next execution time based on current time, scheduled time, and repeat interval
     */
    public void calculateNextExecution() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayScheduled = LocalDateTime.of(now.toLocalDate(), scheduledTime);

        if (lastTriggered == null) {
            // First time: schedule for today if time hasn't passed, otherwise tomorrow
            if (now.isBefore(todayScheduled)) {
                nextExecution = todayScheduled;
            } else {
                nextExecution = calculateNextFromDateTime(todayScheduled);
            }
        } else {
            // Calculate based on last trigger and repeat interval
            nextExecution = calculateNextFromDateTime(lastTriggered);
        }
    }

    /**
     * Calculates the next execution from a given datetime based on repeat interval
     */
    private LocalDateTime calculateNextFromDateTime(LocalDateTime fromDateTime) {
        return switch (repeatInterval) {
            case ONCE -> null; // One-time reminder, don't reschedule
            case DAILY -> fromDateTime.plusDays(1).with(scheduledTime);
            case WEEKLY -> fromDateTime.plusWeeks(1).with(scheduledTime);
            case MONTHLY -> fromDateTime.plusMonths(1).with(scheduledTime);
            case YEARLY -> fromDateTime.plusYears(1).with(scheduledTime);
            case EVERY_HOUR -> fromDateTime.plusHours(1);
            case EVERY_2_HOURS -> fromDateTime.plusHours(2);
            case EVERY_4_HOURS -> fromDateTime.plusHours(4);
            case EVERY_6_HOURS -> fromDateTime.plusHours(6);
            case EVERY_12_HOURS -> fromDateTime.plusHours(12);
        };
    }

    /**
     * Marks this reminder as triggered and calculates next execution
     */
    public void markAsTriggered() {
        this.lastTriggered = LocalDateTime.now();
        calculateNextExecution();
    }

    /**
     * Enum representing different repeat intervals for reminders
     */
    public enum RepeatInterval {
        ONCE("Una vez"),
        DAILY("Diario"),
        WEEKLY("Semanal"),
        MONTHLY("Mensual"),
        YEARLY("Anual"),
        EVERY_HOUR("Cada hora"),
        EVERY_2_HOURS("Cada 2 horas"),
        EVERY_4_HOURS("Cada 4 horas"),
        EVERY_6_HOURS("Cada 6 horas"),
        EVERY_12_HOURS("Cada 12 horas");

        private final String displayName;

        RepeatInterval(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
