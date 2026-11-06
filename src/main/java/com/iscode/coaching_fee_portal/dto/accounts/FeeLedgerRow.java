package com.iscode.coaching_fee_portal.dto.accounts;

import com.iscode.coaching_fee_portal.dto.InstalmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeLedgerRow(
        Long studentId,
        String studentCode,
        String studentName,
        String batch,
        Long feePlanId,
        String academicYear,
        Long instalmentId,
        Long instalmentNumber,
        LocalDate dueDate,
        BigDecimal amountDue,
        BigDecimal amountPaid,
        BigDecimal outstandingAmount,
        InstalmentStatus status,
        long daysOverdue
) {
}