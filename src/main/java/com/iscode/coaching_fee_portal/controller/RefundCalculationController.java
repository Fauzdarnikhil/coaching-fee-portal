package com.iscode.coaching_fee_portal.controller;

import com.iscode.coaching_fee_portal.dto.RefundCalculationResponse;
import com.iscode.coaching_fee_portal.service.RefundCalculationService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/accounts/refunds")
public class RefundCalculationController {

    private final RefundCalculationService refundCalculationService;

    public RefundCalculationController(
            RefundCalculationService refundCalculationService
    ) {
        this.refundCalculationService = refundCalculationService;
    }

    @GetMapping("/calculate")
    public ResponseEntity<RefundCalculationResponse> calculateRefund(
            @RequestParam Long feePlanId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate withdrawalDate
    ) {

        RefundCalculationResponse response =
                refundCalculationService.calculateRefund(
                        feePlanId,
                        withdrawalDate
                );

        return ResponseEntity.ok(response);
    }
}