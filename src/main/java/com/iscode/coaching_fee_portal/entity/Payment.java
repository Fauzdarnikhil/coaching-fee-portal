package com.iscode.coaching_fee_portal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "amount_received", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountReceived;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "transaction_reference", nullable = false, length = 50, unique = true)
    private String transactionReference;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Payment(
            Student student,
            BigDecimal amountReceived,
            PaymentMethod paymentMethod,
            String transactionReference
    ) {
        this.student = student;
        this.amountReceived = amountReceived;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
        this.paymentStatus = PaymentStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void markSuccessful() {
        this.paymentStatus = PaymentStatus.SUCCESS;
        this.receivedAt = LocalDateTime.now();
    }



}
