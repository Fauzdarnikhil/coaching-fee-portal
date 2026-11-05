package com.iscode.coaching_fee_portal.dto.payment;

import com.iscode.coaching_fee_portal.entity.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class InitiatePaymentRequest {

    private Long instalmentId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
}