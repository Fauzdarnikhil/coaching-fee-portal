package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.Instalment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstalmentRepository extends JpaRepository<Instalment, Long> {

    List<Instalment> findByFeePlanIdOrderByInstalmentNumberAsc(Long feePlanId);

    Optional<Instalment> findByFeePlanIdAndInstalmentNumber(
            Long feePlanId,
            Integer instalmentNumber
    );
}
