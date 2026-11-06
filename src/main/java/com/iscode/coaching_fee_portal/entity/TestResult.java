package com.iscode.coaching_fee_portal.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_results")
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String testName;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal marksObtained;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal maximumMarks;

    @Column(nullable = false)
    private LocalDate testDate;

    private LocalDateTime createdAt;

    public TestResult() {
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public BigDecimal getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(BigDecimal marksObtained) {
        this.marksObtained = marksObtained;
    }

    public BigDecimal getMaximumMarks() {
        return maximumMarks;
    }

    public void setMaximumMarks(BigDecimal maximumMarks) {
        this.maximumMarks = maximumMarks;
    }

    public LocalDate getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDate testDate) {
        this.testDate = testDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}