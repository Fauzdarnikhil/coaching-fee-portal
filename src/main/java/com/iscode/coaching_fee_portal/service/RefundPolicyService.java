
package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.entity.RefundPolicy;
import com.iscode.coaching_fee_portal.repository.RefundPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class RefundPolicyService {

    private final RefundPolicyRepository refundPolicyRepository;

    public RefundPolicyService(RefundPolicyRepository refundPolicyRepository) {
        this.refundPolicyRepository = refundPolicyRepository;
    }

    @Transactional(readOnly = true)
    public RefundPolicy getApplicablePolicy(LocalDate withdrawalDate) {

        if (withdrawalDate == null) {
            throw new IllegalArgumentException("Withdrawal date is required");
        }

        return refundPolicyRepository
                .findFirstByActiveTrueAndEffectiveFromLessThanEqualAndEffectiveUntilGreaterThanEqualOrderByEffectiveFromDesc(
                        withdrawalDate,
                        withdrawalDate
                )
                .or(() -> refundPolicyRepository
                        .findFirstByActiveTrueAndEffectiveFromLessThanEqualAndEffectiveUntilIsNullOrderByEffectiveFromDesc(
                                withdrawalDate
                        ))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No active refund policy exists for this date"
                        )
                );
    }
}
