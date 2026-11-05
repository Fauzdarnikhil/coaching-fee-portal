package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.entity.OtpVerification;
import com.iscode.coaching_fee_portal.entity.Parent;
import com.iscode.coaching_fee_portal.repository.OtpVerificationRepository;
import com.iscode.coaching_fee_portal.repository.ParentRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class OtpService {
    private final ParentRepository parentRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();


    public  OtpService(ParentRepository parentRepository,
                       OtpVerificationRepository otpVerificationRepository,
                       BCryptPasswordEncoder passwordEncoder){
        this.parentRepository = parentRepository;
        this.otpVerificationRepository = otpVerificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void generateOTP(String mobileNo){
        Parent parent = parentRepository.findByMobileNo(mobileNo)
                .orElseThrow(() -> new IllegalArgumentException(" Parent not registered."));

        Optional<OtpVerification> previousOtp = otpVerificationRepository
                .findTopByParentIdOrderByCreatedAtDesc(parent.getId());

        if(previousOtp.isPresent()){
            if(previousOtp.get().isUsable()){
                previousOtp.get().invalidate();
            }
        }

        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        String otpHash = passwordEncoder.encode(otp);

        OtpVerification otpVerification = new OtpVerification(parent,otpHash);
        otpVerificationRepository.save(otpVerification);

        System.out.println("Mock OTP for " + mobileNo + ": " + otp);
    }

    @Transactional
    public boolean verifyOTP(String mobileNo, String enteredOtp){
        Parent parent = parentRepository.findByMobileNo(mobileNo)
                .orElseThrow(() -> new IllegalArgumentException("Parent not registered"));

        OtpVerification latestOtp = otpVerificationRepository
                .findTopByParentIdOrderByCreatedAtDesc(parent.getId())
                .orElseThrow(() -> new IllegalArgumentException("OTP not requested"));

        if (!latestOtp.isUsable()) {
            return false;
        }

        boolean check = passwordEncoder.matches(enteredOtp, latestOtp.getOtpHash());

        if (check) {
            latestOtp.markVerified();
            return true;
        }

        latestOtp.incrementAttempt();
        return false;

    }




}
