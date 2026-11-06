package com.iscode.coaching_fee_portal.dto.accounts;
import java.math.BigDecimal;
import java.util.List;

public record StudentDetailResponse(
        Long StudentId,
        String studentCode,
        String studentName,
        String batch,
        String centreName,
        boolean active,
        List<FeePlanDetail> feePlans,
        List<PaymentHistoryResponse> paymentHistory
) {

    public record FeePlanDetail(
            Long feePlanId,
            String academicYear,
            BigDecimal totalFee,
            BigDecimal discountAmount,
            BigDecimal netFee,
            List<InstalmentDetail> instalments
    ) {}

    public record InstalmentDetail(
            Long instalmentId,
            Long instalmentNumber,
            BigDecimal amountDue,
            BigDecimal amountPaid,
            BigDecimal outstandingAmount,
            String dueDate,
            String status,
            long daysOverdue
    ) {}
}
