package com.bydaffi.anypetbackend.scheduler;

import com.bydaffi.anypetbackend.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for processing reminders and sending push notifications.
 * Runs periodically to check for due reminders and trigger notifications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ReminderService reminderService;

    /**
     * Processes due reminders every minute.
     * This checks for any reminders that are scheduled to be sent
     * and triggers the push notifications.
     *
     * Runs at the start of every minute (e.g., 10:00:00, 10:01:00, 10:02:00)
     */
    @Scheduled(cron = "0 * * * * *")
    public void processReminders() {
        log.debug("Running reminder processing task");
        try {
            reminderService.processDueReminders();
        } catch (Exception e) {
            log.error("Error in reminder processing task: {}", e.getMessage(), e);
        }
    }

    /**
     * Alternative: Process reminders at a fixed rate (every 60 seconds)
     * Uncomment this method and comment the one above if you prefer fixed rate over cron
     */
    // @Scheduled(fixedRate = 60000, initialDelay = 5000)
    // public void processRemindersFixedRate() {
    //     log.debug("Running reminder processing task (fixed rate)");
    //     try {
    //         reminderService.processDueReminders();
    //     } catch (Exception e) {
    //         log.error("Error in reminder processing task: {}", e.getMessage(), e);
    //     }
    // }
}
