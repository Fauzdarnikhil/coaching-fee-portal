package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.InstalmentLedgerResponse;
import com.iscode.coaching_fee_portal.dto.InstalmentStatus;
import com.iscode.coaching_fee_portal.entity.Instalment;
import com.iscode.coaching_fee_portal.entity.PaymentAllocation;
import com.iscode.coaching_fee_portal.entity.PaymentStatus;
import com.iscode.coaching_fee_portal.repository.FeePlanRepository;
import com.iscode.coaching_fee_portal.repository.InstalmentRepository;
import com.iscode.coaching_fee_portal.repository.PaymentAllocationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeeLedgerService {
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final InstalmentRepository instalmentRepository;
    private final FeePlanRepository feePlanRepository;

    public FeeLedgerService(PaymentAllocationRepository paymentAllocationRepository,
                            InstalmentRepository instalmentRepository,
                            FeePlanRepository feePlanRepository){

        this.paymentAllocationRepository = paymentAllocationRepository;
        this.instalmentRepository = instalmentRepository;
        this.feePlanRepository = feePlanRepository;
    }

    public  BigDecimal calculateTotalAllocated(Long instalmentId){
        BigDecimal totalAllocated = BigDecimal.ZERO;

        List<PaymentAllocation> payments = paymentAllocationRepository.findByInstalmentId(instalmentId);

        for(PaymentAllocation paymentAllocation : payments){
            if(paymentAllocation.getPayment().getPaymentStatus() == PaymentStatus.SUCCESS){
                totalAllocated = totalAllocated.add(paymentAllocation.getAllocatedAmount());
            }
        }

        return totalAllocated;
    }

    public InstalmentLedgerResponse getInstalmentLedger(Long instalmentId){
        Instalment instalment = instalmentRepository.findById(instalmentId)
                .orElseThrow(() -> new RuntimeException("Instalment Not Found"));

        return buildInstalmentLedger(instalment);
    }

    public List<InstalmentLedgerResponse> getFeePlanLedger(Long feePlanId){
        feePlanRepository.findById(feePlanId)
                .orElseThrow(() -> new RuntimeException("No Fee Plan from this Id"));

        List<Instalment> instalments = instalmentRepository.findByFeePlanIdOrderByInstalmentNumberAsc(feePlanId);

        List<InstalmentLedgerResponse> responses = new ArrayList<>();

        for(Instalment instalment : instalments){
            responses.add(buildInstalmentLedger(instalment));
        }
        return responses;
    }

    private InstalmentStatus determineStatus(BigDecimal amountPaid,
                                             BigDecimal outstandingAmount,
                                             LocalDate dueDate){

        if(outstandingAmount.compareTo(BigDecimal.ZERO) == 0){
            return InstalmentStatus.PAID;
        }
        else if(amountPaid.compareTo(BigDecimal.ZERO) > 0){
            return InstalmentStatus.PARTIALLY_PAID;
        }
        else if(dueDate.isBefore(LocalDate.now())){
            return InstalmentStatus.OVERDUE;
        }

        return InstalmentStatus.UPCOMING;
    }

    private InstalmentLedgerResponse buildInstalmentLedger(
            Instalment instalment) {

        BigDecimal amountPaid = calculateTotalAllocated(instalment.getId());
        BigDecimal outstandingAmount = instalment.getAmountDue().subtract(amountPaid);

        if (outstandingAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Allocated payments exceed instalment amount due"
            );
        }

        InstalmentLedgerResponse instalmentLedgerResponse = new InstalmentLedgerResponse(
                instalment.getId(),
                instalment.getInstalmentNumber(),
                instalment.getAmountDue(),
                amountPaid,
                outstandingAmount,
                instalment.getDueDate(),
                determineStatus(amountPaid, outstandingAmount, instalment.getDueDate())
        );

        return instalmentLedgerResponse;
    }
}
