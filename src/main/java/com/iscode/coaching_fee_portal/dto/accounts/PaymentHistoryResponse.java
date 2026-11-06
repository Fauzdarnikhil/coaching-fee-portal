
package com.iscode.coaching_fee_portal.dto.accounts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentHistoryResponse(
        Long paymentId,
        String transactionReference,
        BigDecimal amountReceived,
        String paymentMethod,
        String paymentStatus,
        LocalDateTime receivedAt,
        LocalDateTime createdAt,
        BigDecimal allocatedAmount,
        BigDecimal unallocatedAmount,
        List<AllocationDetail> allocations
) {
    public record AllocationDetail(
            Long instalmentId,
            Long instalmentNumber,
            BigDecimal allocatedAmount
    ) {}
}
