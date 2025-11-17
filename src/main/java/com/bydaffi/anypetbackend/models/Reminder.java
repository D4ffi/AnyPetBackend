package com.bydaffi.anypetbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity representing a reminder for pet-related activities.
 * Reminders can be scheduled at specific times and can repeat at defined intervals.
 */
@Entity
@Table(name = "reminders")
@Getter
@Setter
@NoArgsConstructor
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Firebase document ID for synchronization with Firestore
     */
    @Column(unique = true)
    private String firebaseId;

    /**
     * Title of the reminder (e.g., "Alimentar a Luna", "Pasear a Rocky")
     */
    @Column(nullable = false)
    private String title;

    /**
     * Detailed description or message for the reminder
     */
    @Column(length = 500)
    private String message;

    /**
     * Time of day when the reminder should trigger (HH:mm format)
     */
    @Column(nullable = false)
    private LocalTime scheduledTime;

    /**
     * Interval for repeating the reminder
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepeatInterval repeatInterval;

    /**
     * User ID who owns this reminder (Firebase UID)
     */
    @Column(nullable = false)
    private String userId;

    /**
     * Optional: associated pet ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    /**
     * FCM device token for sending push notifications
     */
    @Column(nullable = false)
    private String deviceToken;

    /**
     * Whether the reminder is currently active
     */
    @Column(nullable = false)
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
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (nextExecution == null) {
            calculateNextExecution();
        }
    }

    @PreUpdate
    protected void onUpdate() {
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
