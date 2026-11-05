package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.FeePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeePlanRepository extends JpaRepository<FeePlan, Long> {
    List<FeePlan> findByStudentId(Long studentId);

    Optional<FeePlan> findByStudentIdAndAcademicYear(Long studentId, String academicYear);


}
