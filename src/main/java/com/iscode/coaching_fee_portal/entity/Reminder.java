
package com.iscode.coaching_fee_portal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "reminders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "reminder_parent_instalment_type_date",
                        columnNames = {
                                "parent_id",
                                "instalment_id",
                                "reminder_type",
                                "scheduled_date"
                        }
                )
        }
)
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instalment_id", nullable = false)
    private Instalment instalment;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false, length = 20)
    private ReminderType reminderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReminderLanguage language;

    @Column(nullable = false, length = 13)
    private String recipientMobile;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReminderStatus status;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Reminder(
            Parent parent,
            Instalment instalment,
            ReminderType reminderType,
            ReminderLanguage language,
            String recipientMobile,
            String message,
            ReminderStatus status,
            LocalDate scheduledDate
    ) {
        this.parent = parent;
        this.instalment = instalment;
        this.reminderType = reminderType;
        this.language = language;
        this.recipientMobile = recipientMobile;
        this.message = message;
        this.status = status;
        this.scheduledDate = scheduledDate;

        if (status == ReminderStatus.SENT) {
            this.sentAt = LocalDateTime.now();
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
