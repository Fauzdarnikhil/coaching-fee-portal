package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.config.JwtService;
import com.iscode.coaching_fee_portal.dto.auth.AccountsLoginRequest;
import com.iscode.coaching_fee_portal.entity.AccountsUser;
import com.iscode.coaching_fee_portal.repository.AccountsUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountsAuthService {

    private final AccountsUserRepository accountsUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Map<String, String> login(AccountsLoginRequest request) {

        AccountsUser user = accountsUserRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password"));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is disabled");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                user.getId(),
                "ACCOUNTS"
        );

        return Map.of(
                "message", "Login successful",
                "token", token,
                "name", user.getName(),
                "role", user.getRole()
        );
    }
}