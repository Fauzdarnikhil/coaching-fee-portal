
package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.accounts.PaymentHistoryResponse;
import com.iscode.coaching_fee_portal.dto.accounts.PaymentHistoryResponse.AllocationDetail;
import com.iscode.coaching_fee_portal.entity.Payment;
import com.iscode.coaching_fee_portal.entity.PaymentAllocation;
import com.iscode.coaching_fee_portal.repository.PaymentRepository;
import com.iscode.coaching_fee_portal.repository.PaymentAllocationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentHistoryService {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;

    public PaymentHistoryService(
            PaymentRepository paymentRepository,
            PaymentAllocationRepository allocationRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getStudentPaymentHistory(Long studentId) {

        List<Payment> payments =
                paymentRepository.findByStudentIdOrderByCreatedAtDesc(studentId);

        return payments.stream()
                .map(this::mapPayment)
                .toList();
    }

    private PaymentHistoryResponse mapPayment(Payment payment) {

        List<PaymentAllocation> allocations =
                allocationRepository.findByPaymentId(payment.getId());

        // Only successful payments count as allocated collections.
        List<PaymentAllocation> successfulAllocations =
                payment.getPaymentStatus().name().equals("SUCCESS")
                        ? allocations
                        : List.of();

        List<AllocationDetail> allocationDetails =
                successfulAllocations.stream()
                        .map(allocation -> new AllocationDetail(
                                allocation.getInstalment().getId(),
                                allocation.getInstalment()
                                        .getInstalmentNumber(),
                                allocation.getAllocatedAmount()
                        ))
                        .toList();

        BigDecimal allocatedAmount = successfulAllocations.stream()
                .map(PaymentAllocation::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unallocatedAmount =
                payment.getAmountReceived().subtract(allocatedAmount);

        if (unallocatedAmount.compareTo(BigDecimal.ZERO) < 0) {
            unallocatedAmount = BigDecimal.ZERO;
        }

        return new PaymentHistoryResponse(
                payment.getId(),
                payment.getTransactionReference(),
                payment.getAmountReceived(),
                payment.getPaymentMethod().name(),
                payment.getPaymentStatus().name(),
                payment.getReceivedAt(),
                payment.getCreatedAt(),
                allocatedAmount,
                unallocatedAmount,
                allocationDetails
        );
    }
}
