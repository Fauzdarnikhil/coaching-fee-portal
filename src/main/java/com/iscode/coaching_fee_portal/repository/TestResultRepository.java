package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {

    List<TestResult> findByStudent_IdOrderByTestDateDesc(Long studentId);
}