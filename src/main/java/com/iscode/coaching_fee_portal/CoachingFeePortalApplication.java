
package com.iscode.coaching_fee_portal;

import org.springframework.scheduling.annotation.EnableScheduling;
import com.iscode.coaching_fee_portal.entity.*;
import com.iscode.coaching_fee_portal.repository.*;
import com.iscode.coaching_fee_portal.service.PaymentAllocationService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDate;

@EnableScheduling
@SpringBootApplication
public class CoachingFeePortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                CoachingFeePortalApplication.class,
                args
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
            ParentStudentRepository parentStudentRepository,
            RefundPolicyRepository refundPolicyRepository
    ) {

        return args -> {

            RefundPolicy demoRefundPolicy = refundPolicyRepository
                    .findByPolicyNameAndPolicyVersion(
                            "Standard Pro-Rata Demonstration Policy",
                            "1.0"
                    )
                    .orElseGet(() -> refundPolicyRepository.save(
                            new RefundPolicy(
                                    "Standard Pro-Rata Demonstration Policy",
                                    "1.0",
                                    "PRO_RATA_REMAINING_DAYS",
                                    BigDecimal.ZERO,
                                    10,
                                    LocalDate.of(2026, 1, 1),
                                    null
                            )
                    ));

            Centre centre = centreRepository.findByCode("JPR-01")
                    .orElseGet(() -> centreRepository.save(
                            new Centre(
                                    "JPR-01",
                                    "Jaipur Main Centre",
                                    "Jaipur, Rajasthan"
                            )
                    ));

            Student aarav = seedStudent(
                    studentRepository,
                    centre,
                    "STU-0412",
                    "Aarav Meena",
                    "JEE 2027 Morning"
            );

            Parent aaravParent = seedParent(
                    parentRepository,
                    "9876543210",
                    "Aarav's Father",
                    "aarav.parent@example.com",
                    ReminderLanguage.ENGLISH
            );

            ParentStudent aaravParentStudent = linkParent(
                    parentStudentRepository,
                    aaravParent,
                    aarav,
                    "FATHER"
            );

            FeePlan aaravFeePlan = seedFeePlan(
                    feePlanRepository,
                    aarav,
                    "2027"
            );

            Instalment aaravFirstInstalment = seedInstalments(
                    aaravFeePlan,
                    instalmentRepository
            );

            Student ishita = seedStudent(
                    studentRepository,
                    centre,
                    "STU-0587",
                    "Ishita Sharma",
                    "JEE 2027 Evening"
            );

            Parent ishitaParent = seedParent(
                    parentRepository,
                    "9000000001",
                    "Ishita's Father",
                    "ishita.parent@example.com",
                    ReminderLanguage.HINDI
            );

            ParentStudent ishitaParentStudent = linkParent(
                    parentStudentRepository,
                    ishitaParent,
                    ishita,
                    "FATHER"
            );

            FeePlan ishitaFeePlan = seedFeePlan(
                    feePlanRepository,
                    ishita,
                    "2027"
            );

            seedInstalments(
                    ishitaFeePlan,
                    instalmentRepository
            );

            Student rohan = seedStudent(
                    studentRepository,
                    centre,
                    "STU-0621",
                    "Rohan Verma",
                    "JEE 2027 Morning"
            );

            Parent rohanParent = seedParent(
                    parentRepository,
                    "9000000002",
                    "Rohan's Father",
                    "rohan.parent@example.com",
                    ReminderLanguage.ENGLISH
            );

            ParentStudent rohanParentStudent = linkParent(
                    parentStudentRepository,
                    rohanParent,
                    rohan,
                    "FATHER"
            );

            FeePlan rohanFeePlan = seedFeePlan(
                    feePlanRepository,
                    rohan,
                    "2027"
            );

            LocalDate today = LocalDate.now();

            Instalment rohanUpcoming = seedCustomInstalment(
                    rohanFeePlan,
                    instalmentRepository,
                    1L,
                    "30000.00",
                    today.plusDays(3)
            );

            Instalment rohanDueToday = seedCustomInstalment(
                    rohanFeePlan,
                    instalmentRepository,
                    2L,
                    "30000.00",
                    today
            );

            Instalment rohanOverdue = seedCustomInstalment(
                    rohanFeePlan,
                    instalmentRepository,
                    3L,
                    "30000.00",
                    today.minusDays(2)
            );

            seedCustomInstalment(
                    rohanFeePlan,
                    instalmentRepository,
                    4L,
                    "25000.00",
                    today.plusDays(60)
            );

            Student ananya = seedStudent(
                    studentRepository,
                    centre,
                    "STU-0622",
                    "Ananya Gupta",
                    "JEE 2027 Evening"
            );

            Parent ananyaParent = seedParent(
                    parentRepository,
                    "9000000003",
                    "Ananya's Mother",
                    "ananya.parent@example.com",
                    ReminderLanguage.HINDI
            );

            ParentStudent ananyaParentStudent = linkParent(
                    parentStudentRepository,
                    ananyaParent,
                    ananya,
                    "MOTHER"
            );

            FeePlan ananyaFeePlan = seedFeePlan(
                    feePlanRepository,
                    ananya,
                    "2027"
            );

            Instalment ananyaPaidOverdue = seedCustomInstalment(
                    ananyaFeePlan,
                    instalmentRepository,
                    1L,
                    "30000.00",
                    today.minusDays(2)
            );

            seedCustomInstalment(
                    ananyaFeePlan,
                    instalmentRepository,
                    2L,
                    "30000.00",
                    today.plusDays(30)
            );

            seedCustomInstalment(
                    ananyaFeePlan,
                    instalmentRepository,
                    3L,
                    "30000.00",
                    today.plusDays(60)
            );

            seedCustomInstalment(
                    ananyaFeePlan,
                    instalmentRepository,
                    4L,
                    "25000.00",
                    today.plusDays(90)
            );

            Payment aaravPayment = seedSuccessfulPayment(
                    paymentRepository,
                    aarav,
                    "20000.00",
                    PaymentMethod.UPI,
                    "DEMO-TXN-001"
            );

            allocateIfMissing(
                    paymentAllocationRepository,
                    paymentAllocationService,
                    aaravPayment,
                    aaravFirstInstalment,
                    "20000.00"
            );

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

            Payment fullPayment = seedSuccessfulPayment(
                    paymentRepository,
                    aarav,
                    "30000.00",
                    PaymentMethod.UPI,
                    "DEMO-TXN-004"
            );

            Payment rohanPartialPayment = seedSuccessfulPayment(
                    paymentRepository,
                    rohan,
                    "10000.00",
                    PaymentMethod.UPI,
                    "DEMO-ROHAN-PARTIAL"
            );

            allocateIfMissing(
                    paymentAllocationRepository,
                    paymentAllocationService,
                    rohanPartialPayment,
                    rohanOverdue,
                    "10000.00"
            );

            Payment ananyaFullPayment = seedSuccessfulPayment(
                    paymentRepository,
                    ananya,
                    "30000.00",
                    PaymentMethod.UPI,
                    "DEMO-ANANYA-FULL"
            );

            allocateIfMissing(
                    paymentAllocationRepository,
                    paymentAllocationService,
                    ananyaFullPayment,
                    ananyaPaidOverdue,
                    "30000.00"
            );

            System.out.println("========== DEMO DATA ==========");

            System.out.println("Centre ID: " + centre.getId());

            System.out.println("Aarav ID: " + aarav.getId());
            System.out.println("Aarav Code: " + aarav.getStudentCode());
            System.out.println("Aarav Parent ID: " + aaravParent.getId());
            System.out.println("Aarav Parent Mapping ID: " + aaravParentStudent.getId());

            System.out.println("Ishita ID: " + ishita.getId());
            System.out.println("Ishita Code: " + ishita.getStudentCode());
            System.out.println("Ishita Parent ID: " + ishitaParent.getId());
            System.out.println("Ishita Parent Mapping ID: " + ishitaParentStudent.getId());

            System.out.println("Rohan ID: " + rohan.getId());
            System.out.println("Rohan Code: " + rohan.getStudentCode());
            System.out.println("Rohan Parent ID: " + rohanParent.getId());
            System.out.println("Rohan Parent Mapping ID: " + rohanParentStudent.getId());

            System.out.println("Ananya ID: " + ananya.getId());
            System.out.println("Ananya Code: " + ananya.getStudentCode());
            System.out.println("Ananya Parent ID: " + ananyaParent.getId());
            System.out.println("Ananya Parent Mapping ID: " + ananyaParentStudent.getId());

            System.out.println("Rohan Upcoming Instalment ID: " + rohanUpcoming.getId());
            System.out.println("Rohan Due Today Instalment ID: " + rohanDueToday.getId());
            System.out.println("Rohan Overdue Instalment ID: " + rohanOverdue.getId());
            System.out.println("Ananya Paid Instalment ID: " + ananyaPaidOverdue.getId());

            System.out.println("Aarav Payment ID: " + aaravPayment.getId());
            System.out.println("Pending Payment ID: " + pendingPayment.getId());
            System.out.println("Full Payment ID: " + fullPayment.getId());
            System.out.println("Rohan Partial Payment ID: " + rohanPartialPayment.getId());
            System.out.println("Ananya Full Payment ID: " + ananyaFullPayment.getId());

            System.out.println("Refund Policy ID: " + demoRefundPolicy.getId());
            System.out.println("Refund Policy: " + demoRefundPolicy.getPolicyName());

            System.out.println("===============================");
        };
    }

    private Student seedStudent(
            StudentRepository studentRepository,
            Centre centre,
            String studentCode,
            String name,
            String batch
    ) {
        return studentRepository.findByStudentCode(studentCode)
                .orElseGet(() -> studentRepository.save(
                        new Student(
                                studentCode,
                                name,
                                batch,
                                centre
                        )
                ));
    }

    private Parent seedParent(
            ParentRepository parentRepository,
            String mobileNo,
            String name,
            String email,
            ReminderLanguage language
    ) {
        return parentRepository.findByMobileNo(mobileNo)
                .orElseGet(() -> parentRepository.save(
                        new Parent(
                                name,
                                mobileNo,
                                email,
                                language
                        )
                ));
    }

    private ParentStudent linkParent(
            ParentStudentRepository parentStudentRepository,
            Parent parent,
            Student student,
            String relationship
    ) {
        if (parentStudentRepository.existsByParentIdAndStudentId(
                parent.getId(),
                student.getId()
        )) {
            return parentStudentRepository
                    .findByParentIdAndStudentId(
                            parent.getId(),
                            student.getId()
                    )
                    .orElseThrow();
        }

        return parentStudentRepository.save(
                new ParentStudent(
                        parent,
                        student,
                        relationship
                )
        );
    }

    private FeePlan seedFeePlan(
            FeePlanRepository feePlanRepository,
            Student student,
            String academicYear
    ) {
        return feePlanRepository
                .findByStudentIdAndAcademicYear(
                        student.getId(),
                        academicYear
                )
                .orElseGet(() -> feePlanRepository.save(
                        new FeePlan(
                                student,
                                academicYear,
                                new BigDecimal("120000.00"),
                                new BigDecimal("5000.00"),
                                LocalDate.of(2026, 7, 1),
                                LocalDate.of(2027, 6, 30)
                        )
                ));
    }

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

    private Instalment seedCustomInstalment(
            FeePlan feePlan,
            InstalmentRepository instalmentRepository,
            Long number,
            String amount,
            LocalDate dueDate
    ) {
        return instalmentRepository
                .findByFeePlanIdAndInstalmentNumber(
                        feePlan.getId(),
                        number
                )
                .orElseGet(() -> instalmentRepository.save(
                        new Instalment(
                                feePlan,
                                number,
                                new BigDecimal(amount),
                                dueDate
                        )
                ));
    }

    private Payment seedSuccessfulPayment(
            PaymentRepository paymentRepository,
            Student student,
            String amount,
            PaymentMethod method,
            String transactionReference
    ) {
        return paymentRepository
                .findByTransactionReference(transactionReference)
                .orElseGet(() -> {
                    Payment payment = new Payment(
                            student,
                            new BigDecimal(amount),
                            method,
                            transactionReference
                    );

                    payment.markSuccessful();

                    return paymentRepository.save(payment);
                });
    }

    private void allocateIfMissing(
            PaymentAllocationRepository paymentAllocationRepository,
            PaymentAllocationService paymentAllocationService,
            Payment payment,
            Instalment instalment,
            String amount
    ) {
        boolean alreadyAllocated = paymentAllocationRepository
                .findByPaymentIdAndInstalmentId(
                        payment.getId(),
                        instalment.getId()
                )
                .isPresent();

        if (!alreadyAllocated) {
            paymentAllocationService.allocation(
                    payment.getId(),
                    instalment.getId(),
                    new BigDecimal(amount)
            );
        }
    }
}
