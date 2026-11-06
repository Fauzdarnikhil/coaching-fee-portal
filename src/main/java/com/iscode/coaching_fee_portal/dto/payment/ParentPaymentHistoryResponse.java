package com.iscode.coaching_fee_portal.dto.payment;

import com.iscode.coaching_fee_portal.entity.PaymentMethod;
import com.iscode.coaching_fee_portal.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ParentPaymentHistoryResponse {

    private Long paymentId;

    private String studentName;

    private String studentCode;

    private String transactionReference;

    private BigDecimal amountReceived;

    private BigDecimal allocatedAmount;

    private BigDecimal unallocatedAmount;

    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    private Long instalmentNumber;

    private LocalDateTime paymentDate;
}
