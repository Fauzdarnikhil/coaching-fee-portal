
package com.iscode.coaching_fee_portal.controller;

import com.iscode.coaching_fee_portal.dto.accounts.ReminderResponse;
import com.iscode.coaching_fee_portal.service.ReminderService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping("/generate")
    public Map<String, Integer> generateReminders() {

        int generated =
                reminderService.generateDailyReminders();

        return Map.of("generatedCount", generated);
    }

    @GetMapping
    public List<ReminderResponse> getReminderHistory() {
        return reminderService.getReminderHistory();
    }
}
