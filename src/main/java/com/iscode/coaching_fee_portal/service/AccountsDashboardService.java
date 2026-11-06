package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.InstalmentLedgerResponse;
import com.iscode.coaching_fee_portal.dto.accounts.AccountsDashboardResponse;
import com.iscode.coaching_fee_portal.dto.accounts.FeeLedgerRow;
import com.iscode.coaching_fee_portal.entity.*;
import com.iscode.coaching_fee_portal.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountsDashboardService {

    private final StudentRepository studentRepository;
    private final FeePlanRepository feePlanRepository;
    private final InstalmentRepository instalmentRepository;
    private final FeeLedgerService feeLedgerService;

    public AccountsDashboardService(
            StudentRepository studentRepository,
            FeePlanRepository feePlanRepository,
            InstalmentRepository instalmentRepository,
            FeeLedgerService feeLedgerService
    ) {
        this.studentRepository = studentRepository;
        this.feePlanRepository = feePlanRepository;
        this.instalmentRepository = instalmentRepository;
        this.feeLedgerService = feeLedgerService;
    }

    @Transactional(readOnly = true)
    public AccountsDashboardResponse getDashboard() {

        BigDecimal totalFees = BigDecimal.ZERO;
        BigDecimal collectedFees = BigDecimal.ZERO;
        BigDecimal outstandingFees = BigDecimal.ZERO;
        BigDecimal overdueFees = BigDecimal.ZERO;

        List<FeeLedgerRow> rows = new ArrayList<>();

        List<Student> students =
                studentRepository.findByActiveTrueOrderByNameAsc();

        for (Student student : students) {

            List<FeePlan> feePlans =
                    feePlanRepository.findByStudentId(student.getId());

            for (FeePlan feePlan : feePlans) {

                BigDecimal netFee = feePlan.getTotalFee()
                        .subtract(feePlan.getDiscountAmount());

                totalFees = totalFees.add(netFee);

                List<Instalment> instalments =
                        instalmentRepository
                                .findByFeePlanIdOrderByInstalmentNumberAsc(
                                        feePlan.getId()
                                );

                for (Instalment instalment : instalments) {

                    InstalmentLedgerResponse ledger =
                            feeLedgerService.getInstalmentLedger(
                                    instalment.getId()
                            );

                    BigDecimal paid = ledger.getAmountPaid();
                    BigDecimal outstanding =
                            ledger.getOutstandingAmount();

                    collectedFees = collectedFees.add(paid);
                    outstandingFees =
                            outstandingFees.add(outstanding);

                    long daysOverdue = 0;

                    if (outstanding.compareTo(BigDecimal.ZERO) > 0
                            && instalment.getDueDate()
                            .isBefore(LocalDate.now())) {

                        daysOverdue = ChronoUnit.DAYS.between(
                                instalment.getDueDate(),
                                LocalDate.now()
                        );

                        overdueFees = overdueFees.add(outstanding);
                    }

                    rows.add(new FeeLedgerRow(
                            student.getId(),
                            student.getStudentCode(),
                            student.getName(),
                            student.getBatch(),
                            feePlan.getId(),
                            feePlan.getAcademicYear(),
                            instalment.getId(),
                            instalment.getInstalmentNumber(),
                            instalment.getDueDate(),
                            ledger.getAmountDue(),
                            paid,
                            outstanding,
                            ledger.getStatus(),
                            daysOverdue
                    ));
                }
            }
        }

        return new AccountsDashboardResponse(
                totalFees,
                collectedFees,
                outstandingFees,
                overdueFees,
                students.size(),
                rows
        );
    }
}