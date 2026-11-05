
package com.iscode.coaching_fee_portal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "parent_students",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_parent_student",
                        columnNames = {"parent_id", "student_id"}
                )
        }
)
public class ParentStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 20)
    private String relationship;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ParentStudent(Parent parent, Student student, String relationship){
        this.parent = parent;
        this.student = student;
        this.relationship = relationship;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
