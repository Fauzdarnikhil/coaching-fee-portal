package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.Reminder;
import com.iscode.coaching_fee_portal.entity.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    boolean existsByParentIdAndInstalmentIdAndReminderTypeAndScheduledDate(
            Long parentId,
            Long instalmentId,
            ReminderType reminderType,
            LocalDate scheduledDate
    );

    List<Reminder> findByOrderByCreatedAtDesc();
}
