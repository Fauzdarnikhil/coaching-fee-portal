
package com.iscode.coaching_fee_portal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RefundCalculationResponse(
        Long studentId,
        String studentName,
        Long feePlanId,
        String policyName,
        String policyVersion,
        LocalDate withdrawalDate,
        LocalDate courseStartDate,
        LocalDate courseEndDate,
        long totalCourseDays,
        long remainingCourseDays,
        BigDecimal totalEligiblePaid,
        BigDecimal proRataRefund,
        BigDecimal administrativeFee,
        BigDecimal finalRefundAmount,
        LocalDate processingDeadline
) {
}
