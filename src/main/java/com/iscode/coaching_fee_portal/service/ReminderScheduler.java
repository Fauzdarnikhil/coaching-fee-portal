
package com.iscode.coaching_fee_portal.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderScheduler {

    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(
            cron = "0 0 9 * * *",
            zone = "Asia/Kolkata"
    )
    public void sendDailyReminders() {

        int generatedCount = reminderService.generateDailyReminders();

        System.out.println("Daily reminder job completed. Generated reminders: " + generatedCount);
    }
}
