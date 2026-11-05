package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.entity.Instalment;
import com.iscode.coaching_fee_portal.entity.Payment;
import com.iscode.coaching_fee_portal.entity.PaymentAllocation;
import com.iscode.coaching_fee_portal.entity.PaymentStatus;
import com.iscode.coaching_fee_portal.repository.InstalmentRepository;
import com.iscode.coaching_fee_portal.repository.PaymentAllocationRepository;
import com.iscode.coaching_fee_portal.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentAllocationService {
    private final PaymentRepository paymentRepository;
    private final InstalmentRepository instalmentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;

    public PaymentAllocationService(PaymentRepository paymentRepository,
                                    InstalmentRepository instalmentRepository,
                                    PaymentAllocationRepository paymentAllocationRepository){

        this.paymentRepository = paymentRepository;
        this.instalmentRepository = instalmentRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
    }

    @Transactional
    public PaymentAllocation allocation(Long paymentId, Long instalmentId, BigDecimal allocationAmount){
        Payment payment = paymentRepository.findByIdWithLock(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment Not Found"));

        Instalment instalment = instalmentRepository.findByIdWithLock(instalmentId)
                .orElseThrow(() -> new RuntimeException("Instalment Not Found"));

        if (allocationAmount == null) {
            throw new IllegalArgumentException("Allocation amount cannot be null");
        }

        if (allocationAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Allocation amount must be greater than zero"
            );
        }

        if(payment.getPaymentStatus() != PaymentStatus.SUCCESS){
            throw new IllegalArgumentException(
                    "No Successful Payment to calculate Allocation"
            );
        }

        if(payment.getStudent().getId().compareTo(instalment.getFeePlan().getStudent().getId()) != 0){
            throw new IllegalArgumentException("Students are not Same");
        }


        BigDecimal total = totalAllocation(paymentId);

        BigDecimal remainingPayment = payment.getAmountReceived().subtract(total);

        if (remainingPayment.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Existing allocations exceed payment amount"
            );
        }

        if (allocationAmount.compareTo(remainingPayment) > 0) {
            throw new IllegalArgumentException(
                    "Allocation exceeds remaining payment amount"
            );
        }

        BigDecimal totalAllocatedToInstalment = instalmentAllocation(instalmentId);
        BigDecimal outstandingAmount = instalment.getAmountDue().subtract(totalAllocatedToInstalment);

        if (outstandingAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Existing allocations exceed instalment amount");
        }

        if (outstandingAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Instalment is already fully paid");
        }

        if(allocationAmount.compareTo(outstandingAmount) > 0){
            throw new IllegalArgumentException(
                    "Allocation exceeds remaining outstanding Amount"
            );
        }

        PaymentAllocation existingAllocation = paymentAllocationRepository
                .findByPaymentIdAndInstalmentId(paymentId, instalmentId).orElse(null);

        if(existingAllocation != null){
            existingAllocation.setAllocatedAmount(
                    allocationAmount.add(existingAllocation.getAllocatedAmount())
            );

            return paymentAllocationRepository.save(existingAllocation);
        }

        PaymentAllocation paymentAllocation = new PaymentAllocation(payment,instalment,allocationAmount);

        return paymentAllocationRepository.save(paymentAllocation);
    }

    private BigDecimal instalmentAllocation(Long instalmentId) {
        List<PaymentAllocation> allocationList = paymentAllocationRepository.findByInstalmentId(instalmentId);

        BigDecimal total = BigDecimal.ZERO;

        for(PaymentAllocation allocation : allocationList){
            if(allocation.getPayment().getPaymentStatus() == PaymentStatus.SUCCESS){
                total = total.add(allocation.getAllocatedAmount());
            }
        }
        return total;
    }

    private BigDecimal totalAllocation(Long paymentId){
        List<PaymentAllocation> allocationList = paymentAllocationRepository.findByPaymentId(paymentId);

        BigDecimal total = BigDecimal.ZERO;

        for(PaymentAllocation allocation : allocationList){
            if(allocation.getPayment().getPaymentStatus() == PaymentStatus.SUCCESS){
                total = total.add(allocation.getAllocatedAmount());
            }
        }
        return total;
    }
}
