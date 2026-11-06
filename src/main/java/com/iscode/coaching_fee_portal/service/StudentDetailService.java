
package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.accounts.PaymentHistoryResponse;
import com.iscode.coaching_fee_portal.dto.accounts.StudentDetailResponse;
import com.iscode.coaching_fee_portal.dto.accounts.StudentDetailResponse.FeePlanDetail;
import com.iscode.coaching_fee_portal.dto.accounts.StudentDetailResponse.InstalmentDetail;

import com.iscode.coaching_fee_portal.entity.Student;
import com.iscode.coaching_fee_portal.entity.FeePlan;
import com.iscode.coaching_fee_portal.entity.Instalment;

import com.iscode.coaching_fee_portal.repository.StudentRepository;
import com.iscode.coaching_fee_portal.repository.FeePlanRepository;
import com.iscode.coaching_fee_portal.repository.InstalmentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class StudentDetailService {

    private final StudentRepository studentRepository;
    private final FeePlanRepository feePlanRepository;
    private final InstalmentRepository instalmentRepository;
    private final FeeLedgerService feeLedgerService;
    private final PaymentHistoryService paymentHistoryService;

    public StudentDetailService(
            StudentRepository studentRepository,
            FeePlanRepository feePlanRepository,
            InstalmentRepository instalmentRepository,
            FeeLedgerService feeLedgerService,
            PaymentHistoryService paymentHistoryService
    ) {
        this.studentRepository = studentRepository;
        this.feePlanRepository = feePlanRepository;
        this.instalmentRepository = instalmentRepository;
        this.feeLedgerService = feeLedgerService;
        this.paymentHistoryService = paymentHistoryService;
    }

    @Transactional(readOnly = true)
    public StudentDetailResponse getStudentDetails(Long studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Student not found with ID: " + studentId
                        )
                );

        List<FeePlan> feePlans =
                feePlanRepository.findByStudentId(studentId);

        List<FeePlanDetail> feePlanDetails = feePlans.stream()
                .map(this::mapFeePlan)
                .toList();

        List<PaymentHistoryResponse> paymentHistory =
                paymentHistoryService.getStudentPaymentHistory(studentId);

        // 5. Return complete student details
        return new StudentDetailResponse(
                student.getId(),
                student.getStudentCode(),
                student.getName(),
                student.getBatch(),
                student.getCentre().getName(),
                student.isActive(),
                feePlanDetails,
                paymentHistory
        );
    }

    private FeePlanDetail mapFeePlan(FeePlan feePlan) {

        List<Instalment> instalments =
                instalmentRepository.findByFeePlanIdOrderByInstalmentNumberAsc(
                        feePlan.getId()
                );

        List<InstalmentDetail> instalmentDetails = instalments.stream()
                .map(this::mapInstalment)
                .toList();

        BigDecimal netFee = feePlan.getTotalFee()
                .subtract(feePlan.getDiscountAmount());

        return new FeePlanDetail(
                feePlan.getStudent().getId(),
                feePlan.getAcademicYear(),
                feePlan.getTotalFee(),
                feePlan.getDiscountAmount(),
                netFee,
                instalmentDetails
        );
    }

    private InstalmentDetail mapInstalment(Instalment instalment) {

        var ledger =
                feeLedgerService.getInstalmentLedger(instalment.getId());

        long daysOverdue = 0;

        if (ledger.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0
                && ledger.getDueDate().isBefore(LocalDate.now())) {

            daysOverdue = ChronoUnit.DAYS.between(
                    ledger.getDueDate(),
                    LocalDate.now()
            );
        }

        return new InstalmentDetail(
                ledger.getInstalmentId(),
                ledger.getInstalmentNumber(),
                ledger.getAmountDue(),
                ledger.getAmountPaid(),
                ledger.getOutstandingAmount(),
                ledger.getDueDate().toString(),
                ledger.getStatus().name(),
                daysOverdue
        );
    }
}
