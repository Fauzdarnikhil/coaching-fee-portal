package com.iscode.coaching_fee_portal.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

public class InstalmentLedgerResponse {
    private Long instalmentId;
    private Long instalmentNumber;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private BigDecimal outstandingAmount;
    private LocalDate dueDate;
    private InstalmentStatus status;
}
