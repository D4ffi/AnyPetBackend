package com.bydaffi.anypetbackend.repository;

import com.bydaffi.anypetbackend.models.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    /**
     * Find all reminders for a specific user
     * @param userId Firebase UID of the user
     * @return list of reminders for that user
     */
    List<Reminder> findByUserId(String userId);

    /**
     * Find all active reminders for a specific user
     * @param userId Firebase UID of the user
     * @param active active status
     * @return list of active/inactive reminders for that user
     */
    List<Reminder> findByUserIdAndActive(String userId, boolean active);

    /**
     * Find all reminders for a specific pet
     * @param petId pet ID
     * @return list of reminders for that pet
     */
    List<Reminder> findByPetId(Long petId);

    /**
     * Find reminder by Firebase document ID
     * @param firebaseId Firebase document ID
     * @return Optional containing the reminder if found
     */
    Optional<Reminder> findByFirebaseId(String firebaseId);

    /**
     * Find all active reminders that need to be triggered
     * @param now current datetime
     * @return list of reminders ready to be triggered
     */
    @Query("SELECT r FROM Reminder r WHERE r.active = true AND r.nextExecution <= :now")
    List<Reminder> findActiveRemindersDueForExecution(LocalDateTime now);

    /**
     * Find all active reminders
     * @return list of all active reminders
     */
    List<Reminder> findByActive(boolean active);

    /**
     * Delete all reminders for a specific user
     * @param userId Firebase UID of the user
     */
    void deleteByUserId(String userId);
}
