
package com.iscode.coaching_fee_portal.dto.accounts;

import com.iscode.coaching_fee_portal.entity.ReminderStatus;
import com.iscode.coaching_fee_portal.entity.ReminderType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReminderResponse(
        Long id,
        String parentName,
        String recipientMobile,
        String studentName,
        Long instalmentNumber,
        ReminderType reminderType,
        String language,
        String message,
        ReminderStatus status,
        LocalDate scheduledDate,
        LocalDateTime sentAt
) {
}
