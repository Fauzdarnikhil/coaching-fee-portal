package com.iscode.coaching_fee_portal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "parents")

@Entity
public class Parent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, unique = true, length = 13)
    private String mobileNo;

    @Column(length = 30)
    private String email;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReminderLanguage preferredLanguage = ReminderLanguage.ENGLISH;


    public Parent(String name, String mobileNo, String email, ReminderLanguage preferredLanguage) {
        this.name = name;
        this.mobileNo = mobileNo;
        this.email = email;
        this.preferredLanguage = preferredLanguage == null
                ? ReminderLanguage.ENGLISH
                : preferredLanguage;
    }



    @PrePersist
    private void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
