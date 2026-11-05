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
        name = "otp_verifications",
        indexes = {
                @Index(
                        name = "idx_parent_created_at",
                        columnList = "parent_id, created_at"
                )
        }
)
public class OtpVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @Column(nullable = false)
    private String otpHash;

    @Column(nullable = false)
    private Boolean invalidated;

    @Column(nullable = false, updatable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Integer attempt;

    @Column(nullable = false)
    private Boolean verified;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public OtpVerification(Parent parent, String otpHash) {
        this.parent = parent;
        this.otpHash = otpHash;
        this.attempt = 0;
        this.verified = false;
        this.invalidated = false;
    }

    public void incrementAttempt(){
        attempt++;
    }

    public void markVerified(){
        verified = true;
    }

    public void invalidate() {
        this.invalidated = true;
    }

    public boolean isExpired(){
        return !LocalDateTime.now().isBefore(expiresAt);
    }

    public boolean hasExceededAttempts() {
        return attempt >= 5;
    }

    public boolean isUsable() {
        return !verified && !invalidated && !isExpired() && !hasExceededAttempts();
    }

    @PrePersist
    public void onCreated(){
        createdAt = LocalDateTime.now();
        expiresAt = createdAt.plusMinutes(5);
    }
}
