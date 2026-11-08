package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.Instalment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InstalmentRepository extends JpaRepository<Instalment, Long> {

    List<Instalment> findByFeePlanIdOrderByInstalmentNumberAsc(Long feePlanId);

    Optional<Instalment> findByFeePlanIdAndInstalmentNumber(
            Long feePlanId,
            Long instalmentNumber
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Instalment i WHERE i.id = :id")
    Optional<Instalment> findByIdWithLock(@Param("id") Long id);

    List<Instalment> findByFeePlan_Student_Id(Long studentId);
}
