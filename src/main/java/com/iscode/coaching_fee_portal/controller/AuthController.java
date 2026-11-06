
package com.iscode.coaching_fee_portal.controller;

import com.iscode.coaching_fee_portal.config.JwtService;
import com.iscode.coaching_fee_portal.dto.auth.SendOtpRequest;
import com.iscode.coaching_fee_portal.dto.auth.VerifyOtpRequest;
import com.iscode.coaching_fee_portal.entity.Parent;
import com.iscode.coaching_fee_portal.repository.ParentRepository;
import com.iscode.coaching_fee_portal.service.OtpService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OtpService otpService;
    private final ParentRepository parentRepository;
    private final JwtService jwtService;

    public AuthController(
            OtpService otpService,
            ParentRepository parentRepository,
            JwtService jwtService) {

        this.otpService = otpService;
        this.parentRepository = parentRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(
            @RequestBody SendOtpRequest request) {

        try {
            otpService.generateOTP(request.getMobileNo());

            return ResponseEntity.ok(
                    Map.of("message", "OTP sent successfully")
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "message",
                            "This mobile number is not registered with the institute."
                    ));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestBody VerifyOtpRequest request) {

        try {
            boolean verified = otpService.verifyOTP(
                    request.getMobileNo(),
                    request.getOtp()
            );

            if (!verified) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "message",
                                "Invalid or expired OTP. Please try again."
                        ));
            }

            Parent parent = parentRepository
                    .findByMobileNo(request.getMobileNo())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Parent not found"));

            String token = jwtService.generateToken(parent.getId());

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Login successful",
                            "token", token
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "message",
                            "Unable to verify OTP. Please request a new OTP."
                    ));
        }
    }
}
