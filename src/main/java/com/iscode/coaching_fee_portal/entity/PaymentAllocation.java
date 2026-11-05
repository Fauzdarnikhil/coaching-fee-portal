package com.iscode.coaching_fee_portal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(
        name = "payment_allocation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "payment_instalment",
                        columnNames = {"payment_id","instalment_id"}
                )
        }
)
public class PaymentAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instalment_id", nullable = false)
    private Instalment instalment;

    @Setter
    @Column(name = "allocated_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal allocatedAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PaymentAllocation(
            Payment payment,
            Instalment instalment,
            BigDecimal allocatedAmount
    ) {
        this.payment = payment;
        this.instalment = instalment;
        this.allocatedAmount = allocatedAmount;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
