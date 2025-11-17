package com.bydaffi.anypetbackend.controller;

import com.bydaffi.anypetbackend.dto.ReminderRequest;
import com.bydaffi.anypetbackend.dto.ReminderResponse;
import com.bydaffi.anypetbackend.models.Reminder;
import com.bydaffi.anypetbackend.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for managing reminders.
 * Provides endpoints for CRUD operations and Firestore synchronization.
 */
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
@Slf4j
public class ReminderController {

    private final ReminderService reminderService;

    /**
     * Creates a new reminder.
     *
     * Example request:
     * {
     *   "title": "Alimentar a Luna",
     *   "message": "Es hora de alimentar a tu mascota",
     *   "scheduledTime": "09:00",
     *   "repeatInterval": "DAILY",
     *   "userId": "firebase-user-id",
     *   "petId": 1,
     *   "deviceToken": "fcm-device-token",
     *   "active": true
     * }
     *
     * @param request Reminder creation request
     * @return Created reminder
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReminder(@RequestBody ReminderRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate request
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Title is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getScheduledTime() == null) {
                response.put("success", false);
                response.put("message", "Scheduled time is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getRepeatInterval() == null) {
                response.put("success", false);
                response.put("message", "Repeat interval is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getDeviceToken() == null || request.getDeviceToken().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Device token is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Convert DTO to entity
            Reminder reminder = new Reminder();
            reminder.setTitle(request.getTitle());
            reminder.setMessage(request.getMessage());
            reminder.setScheduledTime(LocalTime.parse(request.getScheduledTime()));
            reminder.setRepeatInterval(Reminder.RepeatInterval.valueOf(request.getRepeatInterval()));
            reminder.setUserId(request.getUserId());
            reminder.setPetId(request.getPetId());
            reminder.setDeviceToken(request.getDeviceToken());
            reminder.setActive(request.getActive() != null ? request.getActive() : true);

            Reminder created = reminderService.createReminder(reminder);

            response.put("success", true);
            response.put("message", "Reminder created successfully");
            response.put("reminder", ReminderResponse.fromEntity(created));

            log.info("Created reminder: {} for user: {}", created.getId(), created.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid reminder data: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Invalid data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error creating reminder: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to create reminder: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Updates an existing reminder.
     *
     * @param id Reminder ID (Firebase document ID)
     * @param request Updated reminder data
     * @return Updated reminder
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateReminder(
            @PathVariable String id,
            @RequestBody ReminderRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Convert DTO to entity
            Reminder reminder = new Reminder();
            reminder.setTitle(request.getTitle());
            reminder.setMessage(request.getMessage());
            reminder.setScheduledTime(LocalTime.parse(request.getScheduledTime()));
            reminder.setRepeatInterval(Reminder.RepeatInterval.valueOf(request.getRepeatInterval()));
            reminder.setPetId(request.getPetId());
            reminder.setActive(request.getActive() != null ? request.getActive() : true);
            reminder.setDeviceToken(request.getDeviceToken());
            reminder.setUserId(request.getUserId());

            Reminder updated = reminderService.updateReminder(id, reminder);

            response.put("success", true);
            response.put("message", "Reminder updated successfully");
            response.put("reminder", ReminderResponse.fromEntity(updated));

            log.info("Updated reminder: {}", id);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error updating reminder {}: {}", id, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error updating reminder {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to update reminder: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Deletes a reminder.
     *
     * @param id Reminder ID (Firebase document ID)
     * @return Success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReminder(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();

        try {
            reminderService.deleteReminder(id);

            response.put("success", true);
            response.put("message", "Reminder deleted successfully");

            log.info("Deleted reminder: {}", id);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error deleting reminder {}: {}", id, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error deleting reminder {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to delete reminder: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Gets all reminders for a user.
     *
     * @param userId Firebase UID
     * @return List of reminders
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getRemindersByUserId(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Reminder> reminders = reminderService.getRemindersByUserId(userId);
            List<ReminderResponse> reminderResponses = reminders.stream()
                    .map(ReminderResponse::fromEntity)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("count", reminderResponses.size());
            response.put("reminders", reminderResponses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching reminders for user {}: {}", userId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to fetch reminders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Gets all active reminders for a user.
     *
     * @param userId Firebase UID
     * @return List of active reminders
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<Map<String, Object>> getActiveRemindersByUserId(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Reminder> reminders = reminderService.getActiveRemindersByUserId(userId);
            List<ReminderResponse> reminderResponses = reminders.stream()
                    .map(ReminderResponse::fromEntity)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("count", reminderResponses.size());
            response.put("reminders", reminderResponses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching active reminders for user {}: {}", userId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to fetch active reminders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Gets a reminder by ID.
     *
     * @param id Reminder ID (Firebase document ID)
     * @return Reminder details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReminderById(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Reminder reminder = reminderService.getReminderById(id);

            response.put("success", true);
            response.put("reminder", ReminderResponse.fromEntity(reminder));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error fetching reminder {}: {}", id, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error fetching reminder {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to fetch reminder: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Health check endpoint for reminder service.
     *
     * @return Simple success message
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "Reminder Service");
        return ResponseEntity.ok(response);
    }
}
