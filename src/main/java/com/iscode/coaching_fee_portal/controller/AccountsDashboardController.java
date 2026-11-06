package com.iscode.coaching_fee_portal.controller;

import com.iscode.coaching_fee_portal.dto.accounts.AccountsDashboardResponse;
import com.iscode.coaching_fee_portal.service.AccountsDashboardService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountsDashboardController {

    private final AccountsDashboardService accountsDashboardService;

    public AccountsDashboardController(
            AccountsDashboardService accountsDashboardService
    ) {
        this.accountsDashboardService = accountsDashboardService;
    }

    @GetMapping("/dashboard")
    public AccountsDashboardResponse getDashboard() {
        return accountsDashboardService.getDashboard();
    }
}