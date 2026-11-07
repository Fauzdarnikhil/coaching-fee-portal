
package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.RefundCalculationResponse;
import com.iscode.coaching_fee_portal.entity.*;
import com.iscode.coaching_fee_portal.repository.FeePlanRepository;
import com.iscode.coaching_fee_portal.repository.InstalmentRepository;
import com.iscode.coaching_fee_portal.repository.PaymentAllocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RefundCalculationService {

    private final FeePlanRepository feePlanRepository;
    private final InstalmentRepository instalmentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final RefundPolicyService refundPolicyService;

    public RefundCalculationService(
            FeePlanRepository feePlanRepository,
            InstalmentRepository instalmentRepository,
            PaymentAllocationRepository paymentAllocationRepository,
            RefundPolicyService refundPolicyService
    ) {
        this.feePlanRepository = feePlanRepository;
        this.instalmentRepository = instalmentRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.refundPolicyService = refundPolicyService;
    }

    @Transactional(readOnly = true)
    public RefundCalculationResponse calculateRefund(
            Long feePlanId,
            LocalDate withdrawalDate
    ) {

        FeePlan feePlan = feePlanRepository.findById(feePlanId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Fee plan not found")
                );

        if (withdrawalDate == null) {
            throw new IllegalArgumentException("Withdrawal date is required");
        }

        LocalDate courseStart = feePlan.getCourseStartDate();
        LocalDate courseEnd = feePlan.getCourseEndDate();

        if (withdrawalDate.isBefore(courseStart)
                || withdrawalDate.isAfter(courseEnd)) {
            throw new IllegalArgumentException(
                    "Withdrawal date must be within the course duration"
            );
        }

        RefundPolicy policy =
                refundPolicyService.getApplicablePolicy(withdrawalDate);

        long totalCourseDays =
                ChronoUnit.DAYS.between(courseStart, courseEnd);

        long remainingCourseDays =
                ChronoUnit.DAYS.between(withdrawalDate, courseEnd);

        BigDecimal totalEligiblePaid = calculateEligiblePaid(feePlan);

        BigDecimal proRataRefund = totalEligiblePaid
                .multiply(BigDecimal.valueOf(remainingCourseDays))
                .divide(
                        BigDecimal.valueOf(totalCourseDays),
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal administrativeFee = policy.getAdministrativeFee()
                .min(proRataRefund);

        BigDecimal finalRefundAmount = proRataRefund
                .subtract(administrativeFee);

        return new RefundCalculationResponse(
                feePlan.getStudent().getId(),
                feePlan.getStudent().getName(),
                feePlan.getId(),
                policy.getPolicyName(),
                policy.getPolicyVersion(),
                withdrawalDate,
                courseStart,
                courseEnd,
                totalCourseDays,
                remainingCourseDays,
                totalEligiblePaid,
                proRataRefund,
                administrativeFee,
                finalRefundAmount,
                withdrawalDate.plusDays(policy.getProcessingDeadlineDays())
        );
    }

    private BigDecimal calculateEligiblePaid(FeePlan feePlan) {

        List<Instalment> instalments =
                instalmentRepository.findByFeePlanIdOrderByInstalmentNumberAsc(
                        feePlan.getId()
                );

        BigDecimal totalPaid = BigDecimal.ZERO;

        for (Instalment instalment : instalments) {

            List<PaymentAllocation> allocations =
                    paymentAllocationRepository.findByInstalmentId(
                            instalment.getId()
                    );

            for (PaymentAllocation allocation : allocations) {

                Payment payment = allocation.getPayment();

                if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                    totalPaid = totalPaid.add(
                            allocation.getAllocatedAmount()
                    );
                }
            }
        }

        return totalPaid;
    }
}
