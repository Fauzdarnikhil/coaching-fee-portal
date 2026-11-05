
package com.iscode.coaching_fee_portal.controller;

import com.iscode.coaching_fee_portal.dto.InstalmentLedgerResponse;
import com.iscode.coaching_fee_portal.service.FeeLedgerService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fee-ledger")
public class FeeLedgerController {

    private final FeeLedgerService feeLedgerService;

    public FeeLedgerController(FeeLedgerService feeLedgerService) {
        this.feeLedgerService = feeLedgerService;
    }

    // Get ledger for one instalment
    @GetMapping("/instalments/{instalmentId}")
    public InstalmentLedgerResponse getInstalmentLedger(
            @PathVariable Long instalmentId
    ) {
        return feeLedgerService.getInstalmentLedger(instalmentId);
    }

    // Get ledger for all instalments of a fee plan
    @GetMapping("/plans/{feePlanId}")
    public List<InstalmentLedgerResponse> getFeePlanLedger(
            @PathVariable Long feePlanId
    ) {
        return feeLedgerService.getFeePlanLedger(feePlanId);
    }
}
