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
@Table(
        name = "fee_plan",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "student_academic_year",
                        columnNames = {"student_id", "academic_year"}
                )
        }
)
public class FeePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "total_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalFee;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public FeePlan(
            Student student,
            String academicYear,
            BigDecimal totalFee,
            BigDecimal discountAmount
    ) {
        this.student = student;
        this.academicYear = academicYear;
        this.totalFee = totalFee;
        this.discountAmount = discountAmount == null
                ? BigDecimal.ZERO
                : discountAmount;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
