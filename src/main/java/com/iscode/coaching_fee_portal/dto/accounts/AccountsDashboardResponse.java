package com.iscode.coaching_fee_portal.dto.accounts;

import java.math.BigDecimal;
import java.util.List;

public record AccountsDashboardResponse(
        BigDecimal totalFees,
        BigDecimal collectedFees,
        BigDecimal outstandingFees,
        BigDecimal overdueFees,
        long totalStudents,
        List<FeeLedgerRow> feeLedger
) {
}