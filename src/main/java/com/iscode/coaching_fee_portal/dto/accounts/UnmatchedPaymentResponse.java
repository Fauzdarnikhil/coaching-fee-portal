package com.iscode.coaching_fee_portal.dto.accounts;

import com.iscode.coaching_fee_portal.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UnmatchedPaymentResponse {

    private Long paymentId;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private String transactionReference;
    private BigDecimal amountReceived;
    private BigDecimal allocatedAmount;
    private BigDecimal unallocatedAmount;
    private PaymentMethod paymentMethod;
    private LocalDateTime receivedAt;
}