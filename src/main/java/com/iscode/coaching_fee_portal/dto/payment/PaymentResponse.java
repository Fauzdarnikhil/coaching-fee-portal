package com.iscode.coaching_fee_portal.dto.payment;

import com.iscode.coaching_fee_portal.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentResponse {

    private Long paymentId;
    private String transactionReference;
    private BigDecimal amount;
    private PaymentStatus status;
    private String message;
}