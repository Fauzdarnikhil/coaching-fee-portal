package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.accounts.UnmatchedPaymentResponse;
import com.iscode.coaching_fee_portal.entity.Payment;
import com.iscode.coaching_fee_portal.entity.PaymentAllocation;
import com.iscode.coaching_fee_portal.entity.PaymentStatus;
import com.iscode.coaching_fee_portal.repository.PaymentAllocationRepository;
import com.iscode.coaching_fee_portal.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnmatchedPaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;

    @Transactional(readOnly = true)
    public List<UnmatchedPaymentResponse> getUnmatchedPayments() {

        List<Payment> successfulPayments =
                paymentRepository.findByPaymentStatusOrderByCreatedAtDesc(
                        PaymentStatus.SUCCESS
                );

        return successfulPayments.stream()
                .map(this::mapToResponse)
                .filter(payment ->
                        payment.getUnallocatedAmount()
                                .compareTo(BigDecimal.ZERO) > 0
                )
                .toList();
    }

    private UnmatchedPaymentResponse mapToResponse(Payment payment) {

        List<PaymentAllocation> allocations =
                paymentAllocationRepository.findByPaymentId(payment.getId());

        BigDecimal allocatedAmount = allocations.stream()
                .filter(allocation ->
                        allocation.getPayment().getPaymentStatus()
                                == PaymentStatus.SUCCESS
                )
                .map(PaymentAllocation::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unallocatedAmount =
                payment.getAmountReceived().subtract(allocatedAmount);

        return new UnmatchedPaymentResponse(
                payment.getId(),
                payment.getStudent().getId(),
                payment.getStudent().getName(),
                payment.getStudent().getStudentCode(),
                payment.getTransactionReference(),
                payment.getAmountReceived(),
                allocatedAmount,
                unallocatedAmount,
                payment.getPaymentMethod(),
                payment.getReceivedAt() != null
                        ? payment.getReceivedAt()
                        : payment.getCreatedAt()
        );
    }
}