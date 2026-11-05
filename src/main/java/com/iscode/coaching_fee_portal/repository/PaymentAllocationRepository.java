package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, Long> {
    List<PaymentAllocation> findByInstalmentId(Long InstalmentId);
    List<PaymentAllocation> findByPaymentId(Long paymentId);
}
