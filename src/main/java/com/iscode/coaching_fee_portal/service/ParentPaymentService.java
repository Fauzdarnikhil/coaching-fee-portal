
package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.InstalmentLedgerResponse;
import com.iscode.coaching_fee_portal.dto.payment.InitiatePaymentRequest;
import com.iscode.coaching_fee_portal.dto.payment.PaymentResponse;
import com.iscode.coaching_fee_portal.entity.*;
import com.iscode.coaching_fee_portal.repository.InstalmentRepository;
import com.iscode.coaching_fee_portal.repository.ParentStudentRepository;
import com.iscode.coaching_fee_portal.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ParentPaymentService {

    private final InstalmentRepository instalmentRepository;
    private final PaymentRepository paymentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final FeeLedgerService feeLedgerService;
    private final PaymentAllocationService paymentAllocationService;

    public ParentPaymentService(
            InstalmentRepository instalmentRepository,
            PaymentRepository paymentRepository,
            ParentStudentRepository parentStudentRepository,
            FeeLedgerService feeLedgerService,
            PaymentAllocationService paymentAllocationService) {

        this.instalmentRepository = instalmentRepository;
        this.paymentRepository = paymentRepository;
        this.parentStudentRepository = parentStudentRepository;
        this.feeLedgerService = feeLedgerService;
        this.paymentAllocationService = paymentAllocationService;
    }

    // Parent initiates a partial or full instalment payment
    @Transactional
    public PaymentResponse initiatePayment(
            Long parentId,
            InitiatePaymentRequest request) {

        if (request == null || request.getInstalmentId() == null) {
            throw new IllegalArgumentException(
                    "Instalment ID is required"
            );
        }

        // Validate payment method
        PaymentMethod method = request.getPaymentMethod();

        if ((method != PaymentMethod.UPI &&
                method != PaymentMethod.CARD &&
                method != PaymentMethod.NET_BANKING)) {

            throw new IllegalArgumentException(
                    "Choose UPI, CARD or NET_BANKING"
            );
        }

        // Find instalment
        Instalment instalment = instalmentRepository
                .findById(request.getInstalmentId())
                .orElseThrow(() ->
                        new RuntimeException("Instalment not found"));

        Student student = instalment.getFeePlan().getStudent();

        // Verify that this parent is linked to this student
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

        // Get actual outstanding balance from the fee ledger
        InstalmentLedgerResponse ledger =
                feeLedgerService.getInstalmentLedger(instalment.getId());

        BigDecimal outstandingAmount = ledger.getOutstandingAmount();

        // Parent can enter any positive amount
        BigDecimal payableAmount = request.getAmount();

        if (payableAmount == null ||
                payableAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        // Prevent overpayment
        if (payableAmount.compareTo(outstandingAmount) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount cannot exceed outstanding balance of "
                            + outstandingAmount
            );
        }

        // Generate unique mock transaction reference
        String reference = "MOCK-" + UUID.randomUUID();

        // Create pending payment
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

    // Simulate successful payment gateway callback
    @Transactional
    public PaymentResponse simulateSuccessfulPayment(
            Long parentId,
            Long paymentId) {

        Payment payment = paymentRepository
                .findByIdWithLock(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found"));

        Student student = payment.getStudent();

        // Verify parent ownership again
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

        // Prevent duplicate allocation on repeated callbacks
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

        // Mark payment successful
        payment.markSuccessful();

        // Allocate only the amount actually paid
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
}
