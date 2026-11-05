package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByParentIdOrderByCreatedAtDesc(Long parentId);
}
