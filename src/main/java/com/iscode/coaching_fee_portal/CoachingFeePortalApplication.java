
package com.iscode.coaching_fee_portal;

import com.iscode.coaching_fee_portal.entity.Centre;
import com.iscode.coaching_fee_portal.entity.FeePlan;
import com.iscode.coaching_fee_portal.entity.Student;
import com.iscode.coaching_fee_portal.repository.CentreRepository;
import com.iscode.coaching_fee_portal.repository.FeePlanRepository;
import com.iscode.coaching_fee_portal.repository.StudentRepository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

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
            FeePlanRepository feePlanRepository) {

        return args -> {
            Centre centre = centreRepository.findByCode("JPR-01")
                    .orElseGet(() -> centreRepository.save(
                            new Centre(
                                    "JPR-01",
                                    "Jaipur Main Centre",
                                    "Jaipur, Rajasthan"
                            )
                    ));

            Student student = studentRepository.findByStudentCode("STU-0412")
                    .orElseGet(() -> studentRepository.save(
                            new Student(
                                    "STU-0412",
                                    "Aarav Meena",
                                    "JEE 2027 Morning",
                                    centre
                            )
                    ));

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

            System.out.println("Centre ID: " + centre.getId());
            System.out.println("Student ID: " + student.getId());
            System.out.println("Fee Plan ID: " + feePlan.getId());
        };
    }
}
