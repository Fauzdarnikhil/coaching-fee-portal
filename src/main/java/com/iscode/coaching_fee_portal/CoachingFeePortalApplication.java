
package com.iscode.coaching_fee_portal;

import com.iscode.coaching_fee_portal.entity.Centre;
import com.iscode.coaching_fee_portal.entity.FeePlan;
import com.iscode.coaching_fee_portal.entity.Student;
import com.iscode.coaching_fee_portal.entity.Instalment;
import com.iscode.coaching_fee_portal.entity.Payment;
import com.iscode.coaching_fee_portal.entity.PaymentMethod;
import com.iscode.coaching_fee_portal.entity.PaymentStatus;
import com.iscode.coaching_fee_portal.entity.Parent;
import com.iscode.coaching_fee_portal.entity.ParentStudent;

import com.iscode.coaching_fee_portal.repository.CentreRepository;
import com.iscode.coaching_fee_portal.repository.StudentRepository;
import com.iscode.coaching_fee_portal.repository.FeePlanRepository;
import com.iscode.coaching_fee_portal.repository.InstalmentRepository;
import com.iscode.coaching_fee_portal.repository.PaymentRepository;
import com.iscode.coaching_fee_portal.repository.PaymentAllocationRepository;
import com.iscode.coaching_fee_portal.repository.ParentRepository;
import com.iscode.coaching_fee_portal.repository.ParentStudentRepository;

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
            PaymentAllocationService paymentAllocationService,
            ParentRepository parentRepository,
            ParentStudentRepository parentStudentRepository
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

            // 2. Create or retrieve Aarav
            Student aarav = studentRepository.findByStudentCode("STU-0412")
                    .orElseGet(() -> studentRepository.save(
                            new Student(
                                    "STU-0412",
                                    "Aarav Meena",
                                    "JEE 2027 Morning",
                                    centre
                            )
                    ));

            // 3. Create or retrieve Parent
            Parent parent = parentRepository.findByMobileNo("9876543210")
                    .orElseGet(() -> parentRepository.save(
                            new Parent(
                                    "Aarav's Father",
                                    "9876543210",
                                    "aarav.parent@example.com"
                            )
                    ));

            // 4. Create Parent-Student relationship
            ParentStudent parentStudent;

            if (!parentStudentRepository.existsByParentIdAndStudentId(
                    parent.getId(), aarav.getId())) {

                parentStudent = parentStudentRepository.save(
                        new ParentStudent(
                                parent,
                                aarav,
                                "FATHER"
                        )
                );

            } else {
                parentStudent = parentStudentRepository
                        .findByParentIdAndStudentId(
                                parent.getId(),
                                aarav.getId()
                        )
                        .orElseThrow();
            }

            // 5. Create or retrieve Aarav's Fee Plan
            FeePlan aaravFeePlan = feePlanRepository
                    .findByStudentIdAndAcademicYear(aarav.getId(), "2027")
                    .orElseGet(() -> feePlanRepository.save(
                            new FeePlan(
                                    aarav,
                                    "2027",
                                    new BigDecimal("120000.00"),
                                    new BigDecimal("5000.00")
                            )
                    ));

            // 6. Seed Aarav's four instalments
            Instalment firstInstalment = seedInstalments(
                    aaravFeePlan,
                    instalmentRepository
            );

            // 7. Create or retrieve Ishita
            Student ishita = studentRepository.findByStudentCode("STU-0587")
                    .orElseGet(() -> studentRepository.save(
                            new Student(
                                    "STU-0587",
                                    "Ishita Sharma",
                                    "JEE 2027 Evening",
                                    centre
                            )
                    ));

            // 8. Create or retrieve Ishita's Fee Plan
            FeePlan ishitaFeePlan = feePlanRepository
                    .findByStudentIdAndAcademicYear(ishita.getId(), "2027")
                    .orElseGet(() -> feePlanRepository.save(
                            new FeePlan(
                                    ishita,
                                    "2027",
                                    new BigDecimal("120000.00"),
                                    new BigDecimal("5000.00")
                            )
                    ));

            // 9. Seed all four of Ishita's instalments
            seedInstalments(
                    ishitaFeePlan,
                    instalmentRepository
            );

            // 10. Create or retrieve successful sample payment
            Payment payment = paymentRepository
                    .findByTransactionReference("DEMO-TXN-001")
                    .orElseGet(() -> {

                        Payment newPayment = new Payment(
                                aarav,
                                new BigDecimal("20000.00"),
                                PaymentMethod.UPI,
                                "DEMO-TXN-001"
                        );

                        newPayment.markSuccessful();

                        return paymentRepository.save(newPayment);
                    });

            // 11. Allocate payment to Aarav's first instalment only once
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

            // 12. Create or retrieve pending sample payment
            Payment pendingPayment = paymentRepository
                    .findByTransactionReference("DEMO-TXN-002")
                    .orElseGet(() -> paymentRepository.save(
                            new Payment(
                                    aarav,
                                    new BigDecimal("10000.00"),
                                    PaymentMethod.UPI,
                                    "DEMO-TXN-002"
                            )
                    ));

            // 13. Create or retrieve successful full payment
            Payment fullPayment = paymentRepository
                    .findByTransactionReference("DEMO-TXN-004")
                    .orElseGet(() -> {

                        Payment newPayment = new Payment(
                                aarav,
                                new BigDecimal("30000.00"),
                                PaymentMethod.UPI,
                                "DEMO-TXN-004"
                        );

                        newPayment.markSuccessful();

                        return paymentRepository.save(newPayment);
                    });

            // 14. Print seeded data
            System.out.println("========== DEMO DATA ==========");

            System.out.println("Centre ID: " + centre.getId());

            System.out.println("Aarav ID: " + aarav.getId());
            System.out.println("Aarav Code: " + aarav.getStudentCode());

            System.out.println("Ishita ID: " + ishita.getId());
            System.out.println("Ishita Code: " + ishita.getStudentCode());

            System.out.println("Parent ID: " + parent.getId());
            System.out.println("Parent Name: " + parent.getName());
            System.out.println("Parent Mobile: " + parent.getMobileNo());

            System.out.println("ParentStudent Mapping ID: " + parentStudent.getId());

            System.out.println("Aarav Fee Plan ID: " + aaravFeePlan.getId());
            System.out.println("Ishita Fee Plan ID: " + ishitaFeePlan.getId());

            System.out.println("First Instalment ID: " + firstInstalment.getId());

            System.out.println("Payment ID: " + payment.getId());
            System.out.println("Pending Payment ID: " + pendingPayment.getId());
            System.out.println("Full Payment ID: " + fullPayment.getId());

            System.out.println("===============================");

        };
    }

    // Creates missing instalments without duplicating existing ones
    private Instalment seedInstalments(
            FeePlan feePlan,
            InstalmentRepository instalmentRepository
    ) {

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

        return firstInstalment;
    }
}
