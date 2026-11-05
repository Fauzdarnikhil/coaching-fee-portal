package com.iscode.coaching_fee_portal;

import com.iscode.coaching_fee_portal.entity.Centre;
import com.iscode.coaching_fee_portal.entity.FeePlan;
import com.iscode.coaching_fee_portal.entity.Student;
import com.iscode.coaching_fee_portal.entity.Instalment;
import com.iscode.coaching_fee_portal.entity.Payment;
import com.iscode.coaching_fee_portal.entity.PaymentMethod;
import com.iscode.coaching_fee_portal.entity.PaymentStatus;

import com.iscode.coaching_fee_portal.repository.CentreRepository;
import com.iscode.coaching_fee_portal.repository.StudentRepository;
import com.iscode.coaching_fee_portal.repository.FeePlanRepository;
import com.iscode.coaching_fee_portal.repository.InstalmentRepository;
import com.iscode.coaching_fee_portal.repository.PaymentRepository;
import com.iscode.coaching_fee_portal.repository.PaymentAllocationRepository;

import com.iscode.coaching_fee_portal.service.PaymentAllocationService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootApplication
public class CoachingFeePortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                CoachingFeePortalApplication.class, args
        );
    }

    @Bean
    CommandLineRunner seedData(
            CentreRepository centreRepository,
            StudentRepository studentRepository,
            FeePlanRepository feePlanRepository,
            InstalmentRepository instalmentRepository,
            PaymentRepository paymentRepository,
            PaymentAllocationRepository paymentAllocationRepository,
            PaymentAllocationService paymentAllocationService
    ) {

        return args -> {

            // 1. Create or retrieve Centre

            Centre centre = centreRepository.findByCode("JPR-01")
                    .orElseGet(() -> centreRepository.save(
                            new Centre(
                                    "JPR-01",
                                    "Jaipur Main Centre",
                                    "Jaipur, Rajasthan"
                            )
                    ));

            // 2. Create or retrieve Student

            Student student = studentRepository.findByStudentCode("STU-0412")
                    .orElseGet(() -> studentRepository.save(
                            new Student(
                                    "STU-0412",
                                    "Aarav Meena",
                                    "JEE 2027 Morning",
                                    centre
                            )
                    ));

            // 3. Create or retrieve Fee Plan

            FeePlan feePlan = feePlanRepository
                    .findByStudentIdAndAcademicYear(student.getId(), "2027")
                    .orElseGet(() -> feePlanRepository.save(
                            new FeePlan(
                                    student,
                                    "2027",
                                    new BigDecimal("120000.00"),
                                    new BigDecimal("5000.00")
                            )
                    ));

            // 4. Create or retrieve Instalments

            Long[] numbers = {1L, 2L, 3L, 4L};

            String[] amounts = {
                    "30000.00",
                    "30000.00",
                    "30000.00",
                    "25000.00"
            };

            LocalDate[] dueDates = {
                    LocalDate.of(2026, 10, 10),
                    LocalDate.of(2026, 11, 15),
                    LocalDate.of(2027, 1, 15),
                    LocalDate.of(2027, 3, 15)
            };

            Instalment firstInstalment = null;

            for (int i = 0; i < numbers.length; i++) {

                final int index = i;

                Instalment instalment = instalmentRepository
                        .findByFeePlanIdAndInstalmentNumber(
                                feePlan.getId(),
                                numbers[i]
                        )
                        .orElseGet(() -> instalmentRepository.save(
                                new Instalment(
                                        feePlan,
                                        numbers[index],
                                        new BigDecimal(amounts[index]),
                                        dueDates[index]
                                )
                        ));

                if (numbers[i].equals(1L)) {
                    firstInstalment = instalment;
                }
            }

            // 5. Create or retrieve successful sample payment

            Payment payment = paymentRepository
                    .findByTransactionReference("DEMO-TXN-001")
                    .orElseGet(() -> {

                        Payment newPayment = new Payment(
                                student,
                                new BigDecimal("20000.00"),
                                PaymentMethod.UPI,
                                "DEMO-TXN-001"
                        );

                        newPayment.markSuccessful();

                        return paymentRepository.save(newPayment);
                    });

            // 6. Allocate payment to first instalment only once

            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {

                boolean alreadyAllocated =
                        paymentAllocationRepository
                                .findByPaymentIdAndInstalmentId(
                                        payment.getId(),
                                        firstInstalment.getId()
                                )
                                .isPresent();

                if (!alreadyAllocated) {

                    paymentAllocationService.allocation(
                            payment.getId(),
                            firstInstalment.getId(),
                            new BigDecimal("20000.00")
                    );
                }
            }

            // 7. Print seeded data IDs

            System.out.println("========== DEMO DATA ==========");
            System.out.println("Centre ID: " + centre.getId());
            System.out.println("Student ID: " + student.getId());
            System.out.println("Fee Plan ID: " + feePlan.getId());
            System.out.println("First Instalment ID: " + firstInstalment.getId());
            System.out.println("Payment ID: " + payment.getId());
            System.out.println("Transaction: DEMO-TXN-001");
            System.out.println("===============================");
        };
    }
}
