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
@Table(
        name = "instalments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "student_plan_instalments_number",
                        columnNames = {"fee_plan_id", "instalment_number"}
                )
        }
)
public class Instalment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_plan_id", nullable = false)
    private FeePlan feePlan;

    @Column(name = "instalment_number", nullable = false)
    private Long instalmentNumber;

    @Column(name = "amount_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountDue;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Instalment(
            FeePlan feePlan,
            Long instalmentNumber,
            BigDecimal amountDue,
            LocalDate dueDate
    ) {
        this.feePlan = feePlan;
        this.instalmentNumber = instalmentNumber;
        this.amountDue = amountDue;
        this.dueDate = dueDate;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
