package com.iscode.coaching_fee_portal.dto.accounts;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AllocatePaymentRequest {

    private Long instalmentId;
    private BigDecimal allocationAmount;
}