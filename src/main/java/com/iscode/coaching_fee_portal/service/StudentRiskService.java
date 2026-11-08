package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.StudentRiskResponse;
import com.iscode.coaching_fee_portal.entity.*;
import com.iscode.coaching_fee_portal.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class StudentRiskService {

    private final FeePlanRepository feePlanRepository;
    private final InstalmentRepository instalmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TestResultRepository testResultRepository;
    private final FeeLedgerService feeLedgerService;

    public StudentRiskService(
            FeePlanRepository feePlanRepository,
            InstalmentRepository instalmentRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            TestResultRepository testResultRepository,
            FeeLedgerService feeLedgerService
    ) {
        this.feePlanRepository = feePlanRepository;
        this.instalmentRepository = instalmentRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.testResultRepository = testResultRepository;
        this.feeLedgerService = feeLedgerService;
    }

    @Transactional(readOnly = true)
    public StudentRiskResponse calculateRisk(Student student) {

        int score = 0;

        List<String> riskFactors = new ArrayList<>();

        Long studentId = student.getId();

        boolean feeDataAvailable = false;
        boolean attendanceDataAvailable = false;
        boolean testDataAvailable = false;

        LocalDate today = LocalDate.now();

        // FEE RISK

        int feeRiskPoints = 0;
        String feeRiskFactor = null;

        List<FeePlan> feePlans =
                feePlanRepository.findByStudentId(studentId);

        if (!feePlans.isEmpty()) {
            feeDataAvailable = true;

            for (FeePlan feePlan : feePlans) {

                List<Instalment> instalments =
                        instalmentRepository.findByFeePlanIdOrderByInstalmentNumberAsc(
                                feePlan.getId()
                        );

                for (Instalment instalment : instalments) {

                    BigDecimal outstanding =
                            instalment.getAmountDue().subtract(
                                    feeLedgerService.calculateTotalAllocated(instalment.getId())
                            );

                    if (outstanding.compareTo(BigDecimal.ZERO) <= 0
                            || !instalment.getDueDate().isBefore(today)) {
                        continue;
                    }

                    long overdueDays = ChronoUnit.DAYS.between(
                            instalment.getDueDate(),
                            today
                    );

                    int points = 0;

                    if (overdueDays >= 30) {
                        points = 25;
                    } else if (overdueDays >= 7) {
                        points = 15;
                    }

                    if (points > feeRiskPoints) {
                        feeRiskPoints = points;

                        feeRiskFactor =
                                "Instalment " + instalment.getInstalmentNumber()
                                        + " has an outstanding balance overdue by "
                                        + overdueDays + " days";
                    }
                }
            }
        }

        score += feeRiskPoints;

        if (feeRiskFactor != null) {
            riskFactors.add(feeRiskFactor);
        }

        // ATTENDANCE RISK

        long totalClasses =
                attendanceRecordRepository.countByStudent_Id(studentId);

        if (totalClasses > 0) {
            attendanceDataAvailable = true;

            long presentClasses =
                    attendanceRecordRepository.countByStudent_IdAndStatus(
                            studentId, AttendanceStatus.PRESENT
                    );

            long lateClasses =
                    attendanceRecordRepository.countByStudent_IdAndStatus(
                            studentId, AttendanceStatus.LATE
                    );

            double attendancePercentage =
                    (presentClasses + lateClasses) * 100.0 / totalClasses;

            if (attendancePercentage < 60) {
                score += 30;

                riskFactors.add(
                        "Attendance is below 60% ("
                                + String.format("%.2f", attendancePercentage) + "%)"
                );

            } else if (attendancePercentage < 75) {
                score += 15;

                riskFactors.add(
                        "Attendance is between 60% and 74% ("
                                + String.format("%.2f", attendancePercentage) + "%)"
                );
            }
        }

        // TEST PERFORMANCE RISK

        List<TestResult> testResults =
                testResultRepository.findByStudent_IdOrderByTestDateDesc(studentId);

        if (!testResults.isEmpty()) {
            testDataAvailable = true;

            Map<String, List<TestResult>> resultsBySubject = new HashMap<>();

            for (TestResult result : testResults) {
                resultsBySubject
                        .computeIfAbsent(result.getSubject(), key -> new ArrayList<>())
                        .add(result);
            }

            int testRiskPoints = 0;

            for (Map.Entry<String, List<TestResult>> entry
                    : resultsBySubject.entrySet()) {

                List<TestResult> results = entry.getValue();

                if (results.size() < 2) {
                    continue;
                }

                TestResult latest = results.get(0);
                TestResult previous = results.get(1);

                if (latest.getMaximumMarks().compareTo(BigDecimal.ZERO) <= 0
                        || previous.getMaximumMarks().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                double latestPercentage =
                        latest.getMarksObtained().doubleValue() * 100.0
                                / latest.getMaximumMarks().doubleValue();

                double previousPercentage =
                        previous.getMarksObtained().doubleValue() * 100.0
                                / previous.getMaximumMarks().doubleValue();

                double drop = previousPercentage - latestPercentage;

                int points = 0;

                if (drop >= 20) {
                    points = 25;
                } else if (drop >= 10) {
                    points = 15;
                }

                if (points > testRiskPoints) {
                    testRiskPoints = points;
                }

                if (points > 0) {
                    riskFactors.add(
                            entry.getKey() + " test performance dropped by "
                                    + String.format("%.2f", drop)
                                    + " percentage points"
                    );
                }
            }

            score += testRiskPoints;
        }

        // FINAL RISK CLASSIFICATION

        score = Math.min(score, 100);

        String riskLevel;

        if (score == 0 && !attendanceDataAvailable && !testDataAvailable) {
            riskLevel = "INSUFFICIENT_DATA";
        } else if (score >= 80) {
            riskLevel = "HIGH";
        } else if (score >= 40) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

        return new StudentRiskResponse(
                studentId,
                student.getStudentCode(),
                student.getName(),
                student.getBatch(),
                score,
                riskLevel,
                riskFactors,
                attendanceDataAvailable,
                testDataAvailable,
                feeDataAvailable
        );
    }
}
