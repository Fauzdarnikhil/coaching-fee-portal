
package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.InstalmentLedgerResponse;
import com.iscode.coaching_fee_portal.entity.*;
import com.iscode.coaching_fee_portal.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReminderService {

    private final StudentRepository studentRepository;
    private final FeePlanRepository feePlanRepository;
    private final InstalmentRepository instalmentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final ReminderRepository reminderRepository;
    private final FeeLedgerService feeLedgerService;
    private final ReminderMessageService reminderMessageService;

    public ReminderService(
            StudentRepository studentRepository,
            FeePlanRepository feePlanRepository,
            InstalmentRepository instalmentRepository,
            ParentStudentRepository parentStudentRepository,
            ReminderRepository reminderRepository,
            FeeLedgerService feeLedgerService,
            ReminderMessageService reminderMessageService
    ) {
        this.studentRepository = studentRepository;
        this.feePlanRepository = feePlanRepository;
        this.instalmentRepository = instalmentRepository;
        this.parentStudentRepository = parentStudentRepository;
        this.reminderRepository = reminderRepository;
        this.feeLedgerService = feeLedgerService;
        this.reminderMessageService = reminderMessageService;
    }


    @Transactional(readOnly = true)
    public List<com.iscode.coaching_fee_portal.dto.accounts.ReminderResponse>
    getReminderHistory() {

        return reminderRepository.findByOrderByCreatedAtDesc()
                .stream()
                .map(reminder -> new com.iscode.coaching_fee_portal.dto.accounts.ReminderResponse(
                        reminder.getId(),
                        reminder.getParent().getName(),
                        reminder.getRecipientMobile(),
                        reminder.getInstalment().getFeePlan().getStudent().getName(),
                        reminder.getInstalment().getInstalmentNumber(),
                        reminder.getReminderType(),
                        reminder.getLanguage().name(),
                        reminder.getMessage(),
                        reminder.getStatus(),
                        reminder.getScheduledDate(),
                        reminder.getSentAt()
                ))
                .toList();
    }


    @Transactional
    public int generateDailyReminders() {

        LocalDate today = LocalDate.now();
        int generatedCount = 0;

        List<Student> students =
                studentRepository.findByActiveTrueOrderByNameAsc();

        for (Student student : students) {

            List<FeePlan> feePlans =
                    feePlanRepository.findByStudentId(student.getId());

            List<ParentStudent> parentLinks =
                    parentStudentRepository.findByStudentId(student.getId());

            for (FeePlan feePlan : feePlans) {

                List<Instalment> instalments =
                        instalmentRepository
                                .findByFeePlanIdOrderByInstalmentNumberAsc(
                                        feePlan.getId()
                                );

                for (Instalment instalment : instalments) {

                    LocalDate dueDate = instalment.getDueDate();

                    ReminderType reminderType = null;

                    if (!dueDate.isBefore(today) && !dueDate.isAfter(today.plusDays(3))
                            && !dueDate.equals(today)) {
                        reminderType = ReminderType.UPCOMING;
                    } else if (dueDate.equals(today)) {
                        reminderType = ReminderType.DUE_TODAY;
                    } else if (dueDate.isBefore(today)) {
                        reminderType = ReminderType.OVERDUE;
                    }

                    if (reminderType == null) {
                        continue;
                    }

                    InstalmentLedgerResponse ledger =
                            feeLedgerService.getInstalmentLedger(
                                    instalment.getId()
                            );

                    if (ledger.getOutstandingAmount()
                            .compareTo(java.math.BigDecimal.ZERO) <= 0) {
                        continue;
                    }

                    for (ParentStudent parentLink : parentLinks) {

                        Parent parent = parentLink.getParent();

                        boolean alreadyExists =
                                reminderRepository
                                        .existsByParentIdAndInstalmentIdAndReminderTypeAndScheduledDate(
                                                parent.getId(),
                                                instalment.getId(),
                                                reminderType,
                                                today
                                        );

                        if (alreadyExists) {
                            continue;
                        }

                        String message =
                                reminderMessageService.generateMessage(
                                        parent,
                                        instalment,
                                        reminderType,
                                        ledger.getOutstandingAmount()
                                );

                        Reminder reminder = new Reminder(
                                parent,
                                instalment,
                                reminderType,
                                parent.getPreferredLanguage(),
                                parent.getMobileNo(),
                                message,
                                ReminderStatus.SENT,
                                today
                        );

                        reminderRepository.save(reminder);

                        generatedCount++;
                    }
                }
            }
        }

        return generatedCount;
    }
}
