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

    public Parent(String name, String mobileNo, String email) {
        this.name = name;
        this.mobileNo = mobileNo;
        this.email = email;
    }



    @PrePersist
    private void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
