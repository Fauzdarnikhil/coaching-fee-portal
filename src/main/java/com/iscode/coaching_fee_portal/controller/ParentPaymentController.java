package com.iscode.coaching_fee_portal.controller;

import com.iscode.coaching_fee_portal.dto.payment.InitiatePaymentRequest;
import com.iscode.coaching_fee_portal.dto.payment.ParentPaymentHistoryResponse;
import com.iscode.coaching_fee_portal.dto.payment.PaymentResponse;
import com.iscode.coaching_fee_portal.service.ParentPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parent/payments")
public class ParentPaymentController {

    private final ParentPaymentService parentPaymentService;

    public ParentPaymentController(
            ParentPaymentService parentPaymentService) {
        this.parentPaymentService = parentPaymentService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestBody InitiatePaymentRequest request,
            Authentication authentication) {

        Long parentId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                parentPaymentService.initiatePayment(parentId, request)
        );
    }

    @PostMapping("/{paymentId}/mock-success")
    public ResponseEntity<PaymentResponse> mockSuccess(
            @PathVariable Long paymentId,
            Authentication authentication) {

        Long parentId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                parentPaymentService.simulateSuccessfulPayment(
                        parentId, paymentId
                )
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<ParentPaymentHistoryResponse>> getPaymentHistory(
            Authentication authentication) {

        Long parentId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                parentPaymentService.getPaymentHistory(parentId)
        );
    }
}