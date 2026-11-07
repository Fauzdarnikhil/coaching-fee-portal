
package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.RefundPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {

    List<RefundPolicy> findByActiveTrue();

    Optional<RefundPolicy> findFirstByActiveTrueAndEffectiveFromLessThanEqualAndEffectiveUntilIsNullOrderByEffectiveFromDesc(
            LocalDate date
    );

    Optional<RefundPolicy> findFirstByActiveTrueAndEffectiveFromLessThanEqualAndEffectiveUntilGreaterThanEqualOrderByEffectiveFromDesc(
            LocalDate date,
            LocalDate effectiveUntil
    );

    Optional<RefundPolicy> findByPolicyNameAndPolicyVersion(
            String policyName,
            String policyVersion
    );
}
