package com.iscode.coaching_fee_portal.controller;

import com.iscode.coaching_fee_portal.dto.auth.SendOtpRequest;
import com.iscode.coaching_fee_portal.dto.auth.VerifyOtpRequest;
import com.iscode.coaching_fee_portal.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final OtpService otpService;

    public AuthController(OtpService otpService){
        this.otpService = otpService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody SendOtpRequest otpRequest){
        otpService.generateOTP(otpRequest.getMobileNo());

        return ResponseEntity.ok("OTP Sent");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest verifyOtp){
        boolean verified = otpService.verifyOTP(verifyOtp.getMobileNo(), verifyOtp.getOtp());

        if (!verified) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        return ResponseEntity.ok("OTP Verified");
    }


}
