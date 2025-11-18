package com.bydaffi.anypetbackend.service;

import com.bydaffi.anypetbackend.dto.PushNotificationRequest;
import com.bydaffi.anypetbackend.models.Reminder;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.cloud.Timestamp;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Service for managing reminders with Firebase Firestore as the only storage.
 * Handles CRUD operations and notification scheduling.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final Firestore firestore;
    private final PushNotificationService pushNotificationService;

    private static final String REMINDERS_COLLECTION = "reminders";

    /**
     * Creates a new reminder in Firestore
     *
     * @param reminder Reminder to create
     * @return Created reminder with Firebase ID
     */
    public Reminder createReminder(Reminder reminder) throws ExecutionException, InterruptedException {
        // Initialize timestamps and calculate next execution
        reminder.initializeTimestamps();
        reminder.calculateNextExecution();

        // Create new document in Firestore
        DocumentReference docRef = firestore.collection(REMINDERS_COLLECTION).document();
        reminder.setId(docRef.getId());

        // Save to Firestore
        Map<String, Object> reminderData = convertToFirestoreMap(reminder);
        ApiFuture<WriteResult> result = docRef.set(reminderData);
        result.get(); // Wait for completion

        log.info("Created reminder: {} for user: {}", reminder.getId(), reminder.getUserId());
        return reminder;
    }

    /**
     * Updates an existing reminder in Firestore
     *
     * @param id Reminder ID
     * @param updatedReminder Updated reminder data
     * @return Updated reminder
     */
    public Reminder updateReminder(String id, Reminder updatedReminder) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(REMINDERS_COLLECTION).document(id);

        // Verify the document exists
        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Reminder not found with id: " + id);
        }

        // Update fields
        updatedReminder.setId(id);
        updatedReminder.updateTimestamp();
        updatedReminder.calculateNextExecution();

        // Save to Firestore
        Map<String, Object> reminderData = convertToFirestoreMap(updatedReminder);
        ApiFuture<WriteResult> result = docRef.set(reminderData);
        result.get();

        log.info("Updated reminder: {}", id);
        return updatedReminder;
    }

    /**
     * Deletes a reminder from Firestore
     *
     * @param id Reminder ID
     */
    public void deleteReminder(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(REMINDERS_COLLECTION).document(id);

        // Verify the document exists
        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Reminder not found with id: " + id);
        }

        // Delete from Firestore
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();

        log.info("Deleted reminder: {}", id);
    }

    /**
     * Gets all reminders for a user from Firestore
     *
     * @param userId Firebase UID
     * @return List of reminders
     */
    public List<Reminder> getRemindersByUserId(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(REMINDERS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get();

        QuerySnapshot querySnapshot = future.get();
        return querySnapshot.getDocuments().stream()
                .map(this::convertFromFirestore)
                .collect(Collectors.toList());
    }

    /**
     * Gets all active reminders for a user from Firestore
     *
     * @param userId Firebase UID
     * @return List of active reminders
     */
    public List<Reminder> getActiveRemindersByUserId(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(REMINDERS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("active", true)
                .get();

        QuerySnapshot querySnapshot = future.get();
        return querySnapshot.getDocuments().stream()
                .map(this::convertFromFirestore)
                .collect(Collectors.toList());
    }

    /**
     * Gets a reminder by ID from Firestore
     *
     * @param id Reminder ID
     * @return Reminder
     */
    public Reminder getReminderById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(REMINDERS_COLLECTION).document(id);
        DocumentSnapshot snapshot = docRef.get().get();

        if (!snapshot.exists()) {
            throw new RuntimeException("Reminder not found with id: " + id);
        }

        return convertFromFirestore(snapshot);
    }

    /**
     * Processes due reminders and sends push notifications
     * This method is called by the scheduler
     */
    public void processDueReminders() throws ExecutionException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.of(java.util.Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));

        // Query Firestore for active reminders
        // Note: Filtering by nextExecution in-memory to avoid index requirement
        // TODO: Remove in-memory filtering once Firestore composite index is created
        ApiFuture<QuerySnapshot> future = firestore.collection(REMINDERS_COLLECTION)
                .whereEqualTo("active", true)
                .get();

        QuerySnapshot querySnapshot = future.get();
        List<Reminder> dueReminders = querySnapshot.getDocuments().stream()
                .map(this::convertFromFirestore)
                .filter(reminder -> reminder.getNextExecution() != null &&
                        !reminder.getNextExecution().isAfter(now))
                .collect(Collectors.toList());

        log.info("Processing {} due reminders", dueReminders.size());

        for (Reminder reminder : dueReminders) {
            try {
                sendReminderNotification(reminder);
                reminder.setLastTriggered(LocalDateTime.now());
                reminder.calculateNextExecution();
                reminder.updateTimestamp();

                // Update in Firestore
                DocumentReference docRef = firestore.collection(REMINDERS_COLLECTION).document(reminder.getId());
                Map<String, Object> updates = new HashMap<>();
                updates.put("lastTriggered", Timestamp.of(java.util.Date.from(reminder.getLastTriggered().atZone(ZoneId.systemDefault()).toInstant())));
                updates.put("nextExecution", reminder.getNextExecution() != null ?
                        Timestamp.of(java.util.Date.from(reminder.getNextExecution().atZone(ZoneId.systemDefault()).toInstant())) : null);
                updates.put("updatedAt", Timestamp.of(java.util.Date.from(reminder.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant())));

                docRef.update(updates).get();

                log.info("Successfully processed reminder: {} - Next execution: {}",
                        reminder.getId(), reminder.getNextExecution());
            } catch (Exception e) {
                log.error("Error processing reminder {}: {}", reminder.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Sends a push notification for a reminder
     *
     * @param reminder Reminder to send notification for
     */
    private void sendReminderNotification(Reminder reminder) throws FirebaseMessagingException {
        PushNotificationRequest notificationRequest = new PushNotificationRequest();
        notificationRequest.setTitle(reminder.getTitle());
        notificationRequest.setMessage(reminder.getMessage() != null ? reminder.getMessage() : "Es hora de tu recordatorio");
        notificationRequest.setToken(reminder.getDeviceToken());

        // Note: petName would need to be fetched separately if needed
        // For now, we'll just send the reminder without pet name

        pushNotificationService.sendPushNotification(notificationRequest);

        log.info("Sent notification for reminder: {} to device: {}",
                reminder.getId(), reminder.getDeviceToken());
    }

    /**
     * Converts a Reminder object to a Firestore map
     */
    private Map<String, Object> convertToFirestoreMap(Reminder reminder) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", reminder.getTitle());
        data.put("message", reminder.getMessage());
        data.put("scheduledTime", reminder.getScheduledTime().toString());
        data.put("repeatInterval", reminder.getRepeatInterval().name());
        data.put("userId", reminder.getUserId());
        data.put("petId", reminder.getPetId());
        data.put("deviceToken", reminder.getDeviceToken());
        data.put("active", reminder.isActive());
        data.put("lastTriggered", reminder.getLastTriggered() != null ?
                Timestamp.of(java.util.Date.from(reminder.getLastTriggered().atZone(ZoneId.systemDefault()).toInstant())) : null);
        data.put("nextExecution", reminder.getNextExecution() != null ?
                Timestamp.of(java.util.Date.from(reminder.getNextExecution().atZone(ZoneId.systemDefault()).toInstant())) : null);
        data.put("createdAt", Timestamp.of(java.util.Date.from(reminder.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant())));
        data.put("updatedAt", Timestamp.of(java.util.Date.from(reminder.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant())));

        return data;
    }

    /**
     * Converts a Firestore document to a Reminder object
     */
    private Reminder convertFromFirestore(DocumentSnapshot document) {
        Reminder reminder = new Reminder();
        reminder.setId(document.getId());
        reminder.setTitle(document.getString("title"));
        reminder.setMessage(document.getString("message"));
        reminder.setScheduledTime(LocalTime.parse(document.getString("scheduledTime")));
        reminder.setRepeatInterval(Reminder.RepeatInterval.valueOf(document.getString("repeatInterval")));
        reminder.setUserId(document.getString("userId"));

        Long petId = document.getLong("petId");
        reminder.setPetId(petId);

        reminder.setDeviceToken(document.getString("deviceToken"));
        reminder.setActive(Boolean.TRUE.equals(document.getBoolean("active")));

        Timestamp lastTriggered = document.getTimestamp("lastTriggered");
        if (lastTriggered != null) {
            reminder.setLastTriggered(LocalDateTime.ofInstant(
                    lastTriggered.toDate().toInstant(), ZoneId.systemDefault()));
        }

        Timestamp nextExecution = document.getTimestamp("nextExecution");
        if (nextExecution != null) {
            reminder.setNextExecution(LocalDateTime.ofInstant(
                    nextExecution.toDate().toInstant(), ZoneId.systemDefault()));
        }

        Timestamp createdAt = document.getTimestamp("createdAt");
        if (createdAt != null) {
            reminder.setCreatedAt(LocalDateTime.ofInstant(
                    createdAt.toDate().toInstant(), ZoneId.systemDefault()));
        }

        Timestamp updatedAt = document.getTimestamp("updatedAt");
        if (updatedAt != null) {
            reminder.setUpdatedAt(LocalDateTime.ofInstant(
                    updatedAt.toDate().toInstant(), ZoneId.systemDefault()));
        }

        return reminder;
    }
}
