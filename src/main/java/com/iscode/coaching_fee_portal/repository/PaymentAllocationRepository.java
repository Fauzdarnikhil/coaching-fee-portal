package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.PaymentAllocation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, Long> {
    @EntityGraph(attributePaths = {"payment"})
    List<PaymentAllocation> findByInstalmentId(Long instalmentId);

    @EntityGraph(attributePaths = {"payment"})
    List<PaymentAllocation> findByPaymentId(Long paymentId);

    Optional<PaymentAllocation> findByPaymentIdAndInstalmentId(
            Long paymentId,
            Long instalmentId
    );
}
