package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.Payment;
import com.iscode.coaching_fee_portal.entity.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentId(Long studentId);

    Optional<Payment> findByTransactionReference(String TransactionReference);

    List<Payment> findByStudentIdAndPaymentStatus(Long studentId, PaymentStatus paymentStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdWithLock(@Param("id") Long id);
}
