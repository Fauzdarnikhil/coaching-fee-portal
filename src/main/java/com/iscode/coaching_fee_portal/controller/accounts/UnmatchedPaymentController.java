package com.iscode.coaching_fee_portal.controller.accounts;

import com.iscode.coaching_fee_portal.dto.accounts.AllocatePaymentRequest;
import com.iscode.coaching_fee_portal.dto.accounts.UnmatchedPaymentResponse;
import com.iscode.coaching_fee_portal.service.PaymentAllocationService;
import com.iscode.coaching_fee_portal.service.UnmatchedPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class UnmatchedPaymentController {

    private final UnmatchedPaymentService unmatchedPaymentService;
    private final PaymentAllocationService paymentAllocationService;

    @GetMapping("/unmatched-payments")
    public List<UnmatchedPaymentResponse> getUnmatchedPayments() {
        return unmatchedPaymentService.getUnmatchedPayments();
    }

    @PostMapping("/payments/{paymentId}/allocate")
    public ResponseEntity<Map<String, String>> allocatePayment(
            @PathVariable Long paymentId,
            @RequestBody AllocatePaymentRequest request
    ) {
        paymentAllocationService.allocation(
                paymentId,
                request.getInstalmentId(),
                request.getAllocationAmount()
        );

        return ResponseEntity.ok(
                Map.of("message", "Payment allocated successfully")
        );
    }
}