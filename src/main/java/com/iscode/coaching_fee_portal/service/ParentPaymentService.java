
package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.InstalmentLedgerResponse;
import com.iscode.coaching_fee_portal.dto.payment.InitiatePaymentRequest;
import com.iscode.coaching_fee_portal.dto.payment.PaymentResponse;
import com.iscode.coaching_fee_portal.dto.payment.ParentPaymentHistoryResponse;
import com.iscode.coaching_fee_portal.entity.*;
import com.iscode.coaching_fee_portal.repository.InstalmentRepository;
import com.iscode.coaching_fee_portal.repository.ParentStudentRepository;
import com.iscode.coaching_fee_portal.repository.PaymentRepository;
import com.iscode.coaching_fee_portal.repository.PaymentAllocationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ParentPaymentService {

    private final InstalmentRepository instalmentRepository;
    private final PaymentRepository paymentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final FeeLedgerService feeLedgerService;
    private final PaymentAllocationService paymentAllocationService;

    public ParentPaymentService(
            InstalmentRepository instalmentRepository,
            PaymentRepository paymentRepository,
            ParentStudentRepository parentStudentRepository,
            PaymentAllocationRepository paymentAllocationRepository,
            FeeLedgerService feeLedgerService,
            PaymentAllocationService paymentAllocationService) {

        this.instalmentRepository = instalmentRepository;
        this.paymentRepository = paymentRepository;
        this.parentStudentRepository = parentStudentRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.feeLedgerService = feeLedgerService;
        this.paymentAllocationService = paymentAllocationService;
    }

    @Transactional
    public PaymentResponse initiatePayment(
            Long parentId,
            InitiatePaymentRequest request) {

        if (request == null || request.getInstalmentId() == null) {
            throw new IllegalArgumentException("Instalment ID is required");
        }

        PaymentMethod method = request.getPaymentMethod();

        if (method != PaymentMethod.UPI &&
                method != PaymentMethod.CARD &&
                method != PaymentMethod.NET_BANKING) {

            throw new IllegalArgumentException(
                    "Choose UPI, CARD or NET_BANKING"
            );
        }

        Instalment instalment = instalmentRepository
                .findById(request.getInstalmentId())
                .orElseThrow(() ->
                        new RuntimeException("Instalment not found"));

        Student student = instalment.getFeePlan().getStudent();

        boolean linked = parentStudentRepository
                .existsByParentIdAndStudentId(
                        parentId,
                        student.getId()
                );

        if (!linked) {
            throw new IllegalArgumentException(
                    "You are not authorized to pay for this student"
            );
        }

        InstalmentLedgerResponse ledger =
                feeLedgerService.getInstalmentLedger(instalment.getId());

        BigDecimal outstandingAmount = ledger.getOutstandingAmount();
        BigDecimal payableAmount = request.getAmount();

        if (payableAmount == null ||
                payableAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        if (payableAmount.compareTo(outstandingAmount) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount cannot exceed outstanding balance of "
                            + outstandingAmount
            );
        }

        String reference = "MOCK-" + UUID.randomUUID();

        Payment payment = new Payment(
                student,
                payableAmount,
                method,
                reference,
                instalment
        );

        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResponse(
                savedPayment.getId(),
                savedPayment.getTransactionReference(),
                savedPayment.getAmountReceived(),
                savedPayment.getPaymentStatus(),
                "Mock payment initiated. Click Pay Now to simulate success."
        );
    }

    @Transactional
    public PaymentResponse simulateSuccessfulPayment(
            Long parentId,
            Long paymentId) {

        Payment payment = paymentRepository
                .findByIdWithLock(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found"));

        Student student = payment.getStudent();

        boolean linked = parentStudentRepository
                .existsByParentIdAndStudentId(
                        parentId,
                        student.getId()
                );

        if (!linked) {
            throw new IllegalArgumentException(
                    "You are not authorized to access this payment"
            );
        }

        Instalment intendedInstalment =
                payment.getIntendedInstalment();

        if (intendedInstalment == null) {
            throw new IllegalStateException(
                    "No instalment is associated with this payment"
            );
        }

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return new PaymentResponse(
                    payment.getId(),
                    payment.getTransactionReference(),
                    payment.getAmountReceived(),
                    payment.getPaymentStatus(),
                    "Payment was already successful"
            );
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending payments can be completed"
            );
        }

        payment.markSuccessful();

        paymentAllocationService.allocation(
                payment.getId(),
                intendedInstalment.getId(),
                payment.getAmountReceived()
        );

        return new PaymentResponse(
                payment.getId(),
                payment.getTransactionReference(),
                payment.getAmountReceived(),
                payment.getPaymentStatus(),
                "Mock payment successful and allocated to instalment"
        );
    }

    @Transactional(readOnly = true)
    public List<ParentPaymentHistoryResponse> getPaymentHistory(
            Long parentId) {

        List<ParentStudent> relationships =
                parentStudentRepository.findByParentId(parentId);

        List<ParentPaymentHistoryResponse> history =
                new ArrayList<>();

        for (ParentStudent relationship : relationships) {

            Student student = relationship.getStudent();

            List<Payment> payments =
                    paymentRepository.findByStudentIdOrderByCreatedAtDesc(
                            student.getId()
                    );


            for (Payment payment : payments) {

                List<PaymentAllocation> allocations =
                        paymentAllocationRepository.findByPaymentId(payment.getId());

                List<PaymentAllocation> successfulAllocations =
                        payment.getPaymentStatus() == PaymentStatus.SUCCESS
                                ? allocations
                                : List.of();

                BigDecimal allocatedAmount = successfulAllocations.stream()
                        .map(PaymentAllocation::getAllocatedAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal unallocatedAmount = BigDecimal.ZERO;

                if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                    unallocatedAmount = payment.getAmountReceived()
                            .subtract(allocatedAmount);
                }

                Long instalmentNumber = null;

                if (successfulAllocations.size() == 1) {
                    instalmentNumber = successfulAllocations.get(0)
                            .getInstalment()
                            .getInstalmentNumber();
                }

                history.add(new ParentPaymentHistoryResponse(
                        payment.getId(),
                        student.getName(),
                        student.getStudentCode(),
                        payment.getTransactionReference(),
                        payment.getAmountReceived(),
                        allocatedAmount,
                        unallocatedAmount,
                        payment.getPaymentMethod(),
                        payment.getPaymentStatus(),
                        instalmentNumber,
                        payment.getReceivedAt() != null
                                ? payment.getReceivedAt()
                                : payment.getCreatedAt()
                ));
            }
        }
        return history;
    }
}
