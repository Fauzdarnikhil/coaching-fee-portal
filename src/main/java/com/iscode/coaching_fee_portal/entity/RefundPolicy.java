
package com.iscode.coaching_fee_portal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "refund_policies")
public class RefundPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String policyName;

    @Column(nullable = false, length = 30)
    private String policyVersion;

    @Column(nullable = false, length = 30)
    private String calculationMethod;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal administrativeFee = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer processingDeadlineDays;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveUntil;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RefundPolicy(
            String policyName,
            String policyVersion,
            String calculationMethod,
            BigDecimal administrativeFee,
            Integer processingDeadlineDays,
            LocalDate effectiveFrom,
            LocalDate effectiveUntil
    ) {
        if (policyName == null || policyName.isBlank()) {
            throw new IllegalArgumentException("Policy name is required");
        }

        if (processingDeadlineDays == null || processingDeadlineDays <= 0) {
            throw new IllegalArgumentException("Invalid processing deadline");
        }

        if (effectiveFrom == null ||
                (effectiveUntil != null && effectiveUntil.isBefore(effectiveFrom))) {
            throw new IllegalArgumentException("Invalid policy effective dates");
        }

        this.policyName = policyName;
        this.policyVersion = policyVersion;
        this.calculationMethod = calculationMethod;
        this.administrativeFee = administrativeFee == null
                ? BigDecimal.ZERO
                : administrativeFee;
        this.processingDeadlineDays = processingDeadlineDays;
        this.effectiveFrom = effectiveFrom;
        this.effectiveUntil = effectiveUntil;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
