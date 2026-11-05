package com.iscode.coaching_fee_portal.dto.parent;

import com.iscode.coaching_fee_portal.dto.InstalmentLedgerResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeePlanLedgerInfo {

    private Long feePlanId;
    private String academicYear;
    private BigDecimal totalFee;
    private BigDecimal discountAmount;
    private BigDecimal netFee;
    private List<InstalmentLedgerResponse> instalments;
}