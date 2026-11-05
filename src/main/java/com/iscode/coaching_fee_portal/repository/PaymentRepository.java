package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.Payment;
import com.iscode.coaching_fee_portal.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentId(Long studentId);

    Optional<Payment> findByTransactionReference(String TransactionReference);

    List<Payment> findByStudentIdAndPaymentStatus(Long studentId, PaymentStatus paymentStatus);
}
