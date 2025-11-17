package com.bydaffi.anypetbackend.service;

import com.bydaffi.anypetbackend.dto.PushNotificationRequest;
import com.bydaffi.anypetbackend.models.Reminder;
import com.bydaffi.anypetbackend.repository.ReminderRepository;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Service for managing reminders with Firebase Firestore synchronization.
 * Handles CRUD operations, synchronization with Firestore, and notification scheduling.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final Firestore firestore;
    private final PushNotificationService pushNotificationService;

    private static final String REMINDERS_COLLECTION = "reminders";

    /**
     * Creates a new reminder and synchronizes it with Firestore
     *
     * @param reminder Reminder to create
     * @return Created reminder with Firebase ID
     */
    @Transactional
    public Reminder createReminder(Reminder reminder) throws ExecutionException, InterruptedException {
        // Save to local database first
        reminder.calculateNextExecution();
        Reminder savedReminder = reminderRepository.save(reminder);

        // Sync to Firestore
        syncReminderToFirestore(savedReminder);

        log.info("Created reminder: {} for user: {}", savedReminder.getId(), savedReminder.getUserId());
        return savedReminder;
    }

    /**
     * Updates an existing reminder and synchronizes with Firestore
     *
     * @param id Reminder ID
     * @param updatedReminder Updated reminder data
     * @return Updated reminder
     */
    @Transactional
    public Reminder updateReminder(Long id, Reminder updatedReminder) throws ExecutionException, InterruptedException {
        Reminder existing = reminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reminder not found with id: " + id));

        // Update fields
        existing.setTitle(updatedReminder.getTitle());
        existing.setMessage(updatedReminder.getMessage());
        existing.setScheduledTime(updatedReminder.getScheduledTime());
        existing.setRepeatInterval(updatedReminder.getRepeatInterval());
        existing.setActive(updatedReminder.isActive());
        existing.setDeviceToken(updatedReminder.getDeviceToken());

        // Recalculate next execution
        existing.calculateNextExecution();

        Reminder saved = reminderRepository.save(existing);

        // Sync to Firestore
        syncReminderToFirestore(saved);

        log.info("Updated reminder: {}", saved.getId());
        return saved;
    }

    /**
     * Deletes a reminder from both local DB and Firestore
     *
     * @param id Reminder ID
     */
    @Transactional
    public void deleteReminder(Long id) throws ExecutionException, InterruptedException {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reminder not found with id: " + id));

        // Delete from Firestore
        if (reminder.getFirebaseId() != null) {
            firestore.collection(REMINDERS_COLLECTION)
                    .document(reminder.getFirebaseId())
                    .delete()
                    .get();
        }

        // Delete from local DB
        reminderRepository.delete(reminder);

        log.info("Deleted reminder: {}", id);
    }

    /**
     * Gets all reminders for a user
     *
     * @param userId Firebase UID
     * @return List of reminders
     */
    public List<Reminder> getRemindersByUserId(String userId) {
        return reminderRepository.findByUserId(userId);
    }

    /**
     * Gets all active reminders for a user
     *
     * @param userId Firebase UID
     * @return List of active reminders
     */
    public List<Reminder> getActiveRemindersByUserId(String userId) {
        return reminderRepository.findByUserIdAndActive(userId, true);
    }

    /**
     * Gets a reminder by ID
     *
     * @param id Reminder ID
     * @return Reminder
     */
    public Reminder getReminderById(Long id) {
        return reminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reminder not found with id: " + id));
    }

    /**
     * Synchronizes a reminder to Firebase Firestore
     *
     * @param reminder Reminder to sync
     */
    private void syncReminderToFirestore(Reminder reminder) throws ExecutionException, InterruptedException {
        DocumentReference docRef;

        if (reminder.getFirebaseId() != null) {
            // Update existing document
            docRef = firestore.collection(REMINDERS_COLLECTION).document(reminder.getFirebaseId());
        } else {
            // Create new document
            docRef = firestore.collection(REMINDERS_COLLECTION).document();
            reminder.setFirebaseId(docRef.getId());
            reminderRepository.save(reminder); // Save the Firebase ID to local DB
        }

        Map<String, Object> reminderData = new HashMap<>();
        reminderData.put("id", reminder.getId());
        reminderData.put("title", reminder.getTitle());
        reminderData.put("message", reminder.getMessage());
        reminderData.put("scheduledTime", reminder.getScheduledTime().toString());
        reminderData.put("repeatInterval", reminder.getRepeatInterval().name());
        reminderData.put("userId", reminder.getUserId());
        reminderData.put("petId", reminder.getPet() != null ? reminder.getPet().getId() : null);
        reminderData.put("deviceToken", reminder.getDeviceToken());
        reminderData.put("active", reminder.isActive());
        reminderData.put("lastTriggered", reminder.getLastTriggered() != null ?
                Timestamp.of(java.util.Date.from(reminder.getLastTriggered().atZone(ZoneId.systemDefault()).toInstant())) : null);
        reminderData.put("nextExecution", reminder.getNextExecution() != null ?
                Timestamp.of(java.util.Date.from(reminder.getNextExecution().atZone(ZoneId.systemDefault()).toInstant())) : null);
        reminderData.put("createdAt", Timestamp.of(java.util.Date.from(reminder.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant())));
        reminderData.put("updatedAt", Timestamp.of(java.util.Date.from(reminder.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant())));

        ApiFuture<WriteResult> result = docRef.set(reminderData);
        result.get(); // Wait for completion

        log.info("Synced reminder {} to Firestore with document ID: {}", reminder.getId(), reminder.getFirebaseId());
    }

    /**
     * Synchronizes reminders from Firestore to local database
     * This can be used to pull updates from Firestore
     *
     * @param userId User ID to sync reminders for
     */
    @Transactional
    public void syncFromFirestore(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(REMINDERS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get();

        QuerySnapshot querySnapshot = future.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

        for (QueryDocumentSnapshot document : documents) {
            String firebaseId = document.getId();
            Optional<Reminder> existingOpt = reminderRepository.findByFirebaseId(firebaseId);

            Map<String, Object> data = document.getData();

            if (existingOpt.isPresent()) {
                // Update existing reminder
                Reminder existing = existingOpt.get();
                updateReminderFromFirestoreData(existing, data, firebaseId);
                reminderRepository.save(existing);
            } else {
                // Create new reminder from Firestore data
                Reminder newReminder = createReminderFromFirestoreData(data, firebaseId);
                reminderRepository.save(newReminder);
            }
        }

        log.info("Synced {} reminders from Firestore for user: {}", documents.size(), userId);
    }

    /**
     * Creates a Reminder entity from Firestore document data
     */
    private Reminder createReminderFromFirestoreData(Map<String, Object> data, String firebaseId) {
        Reminder reminder = new Reminder();
        updateReminderFromFirestoreData(reminder, data, firebaseId);
        return reminder;
    }

    /**
     * Updates a Reminder entity with Firestore document data
     */
    private void updateReminderFromFirestoreData(Reminder reminder, Map<String, Object> data, String firebaseId) {
        reminder.setFirebaseId(firebaseId);
        reminder.setTitle((String) data.get("title"));
        reminder.setMessage((String) data.get("message"));
        reminder.setScheduledTime(LocalTime.parse((String) data.get("scheduledTime")));
        reminder.setRepeatInterval(Reminder.RepeatInterval.valueOf((String) data.get("repeatInterval")));
        reminder.setUserId((String) data.get("userId"));
        reminder.setDeviceToken((String) data.get("deviceToken"));
        reminder.setActive((Boolean) data.getOrDefault("active", true));

        // Handle timestamps
        Timestamp lastTriggered = (Timestamp) data.get("lastTriggered");
        if (lastTriggered != null) {
            reminder.setLastTriggered(LocalDateTime.ofInstant(
                    lastTriggered.toDate().toInstant(), ZoneId.systemDefault()));
        }

        Timestamp nextExecution = (Timestamp) data.get("nextExecution");
        if (nextExecution != null) {
            reminder.setNextExecution(LocalDateTime.ofInstant(
                    nextExecution.toDate().toInstant(), ZoneId.systemDefault()));
        }
    }

    /**
     * Processes due reminders and sends push notifications
     * This method is called by the scheduler
     */
    @Transactional
    public void processDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> dueReminders = reminderRepository.findActiveRemindersDueForExecution(now);

        log.info("Processing {} due reminders", dueReminders.size());

        for (Reminder reminder : dueReminders) {
            try {
                sendReminderNotification(reminder);
                reminder.markAsTriggered();
                reminderRepository.save(reminder);

                // Sync updated reminder to Firestore
                syncReminderToFirestore(reminder);

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

        if (reminder.getPet() != null) {
            notificationRequest.setPetName(reminder.getPet().getName());
        }

        pushNotificationService.sendPushNotification(notificationRequest);

        log.info("Sent notification for reminder: {} to device: {}",
                reminder.getId(), reminder.getDeviceToken());
    }
}
