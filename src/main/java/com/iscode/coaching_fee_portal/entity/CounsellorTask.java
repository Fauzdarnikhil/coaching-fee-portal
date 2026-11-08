
package com.iscode.coaching_fee_portal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "counsellor_tasks")
public class CounsellorTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private Integer riskScore;

    @Column(nullable = false, length = 30)
    private String riskLevel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String riskReasons;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CounsellorTaskStatus status = CounsellorTaskStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String falsePositiveReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    public CounsellorTask(
            Student student,
            Integer riskScore,
            String riskLevel,
            String riskReasons
    ) {
        this.student = student;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.riskReasons = riskReasons;
        this.status = CounsellorTaskStatus.OPEN;
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
