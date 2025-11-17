package com.bydaffi.anypetbackend.dto;

import com.bydaffi.anypetbackend.models.Reminder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO for reminder responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReminderResponse {

    private Long id;
    private String firebaseId;
    private String title;
    private String message;
    private String scheduledTime;
    private String repeatInterval;
    private String userId;
    private Long petId;
    private String petName;
    private boolean active;
    private LocalDateTime lastTriggered;
    private LocalDateTime nextExecution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Creates a ReminderResponse from a Reminder entity
     */
    public static ReminderResponse fromEntity(Reminder reminder) {
        ReminderResponse response = new ReminderResponse();
        response.setId(reminder.getId());
        response.setFirebaseId(reminder.getFirebaseId());
        response.setTitle(reminder.getTitle());
        response.setMessage(reminder.getMessage());
        response.setScheduledTime(reminder.getScheduledTime().toString());
        response.setRepeatInterval(reminder.getRepeatInterval().name());
        response.setUserId(reminder.getUserId());
        response.setPetId(reminder.getPet() != null ? reminder.getPet().getId() : null);
        response.setPetName(reminder.getPet() != null ? reminder.getPet().getName() : null);
        response.setActive(reminder.isActive());
        response.setLastTriggered(reminder.getLastTriggered());
        response.setNextExecution(reminder.getNextExecution());
        response.setCreatedAt(reminder.getCreatedAt());
        response.setUpdatedAt(reminder.getUpdatedAt());
        return response;
    }
}
