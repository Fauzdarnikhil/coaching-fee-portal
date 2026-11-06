package com.iscode.coaching_fee_portal.controller;

import com.iscode.coaching_fee_portal.dto.auth.AccountsLoginRequest;
import com.iscode.coaching_fee_portal.service.AccountsAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/accounts")
@RequiredArgsConstructor
public class AccountsAuthController {

    private final AccountsAuthService accountsAuthService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody AccountsLoginRequest request
    ) {
        try {
            return ResponseEntity.ok(
                    accountsAuthService.login(request)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}