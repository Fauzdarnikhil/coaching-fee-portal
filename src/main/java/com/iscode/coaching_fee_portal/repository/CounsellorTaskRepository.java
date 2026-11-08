package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.CounsellorTask;
import com.iscode.coaching_fee_portal.entity.CounsellorTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CounsellorTaskRepository
        extends JpaRepository<CounsellorTask, Long> {

    Optional<CounsellorTask> findFirstByStudent_IdAndStatusInOrderByCreatedAtDesc(
            Long studentId,
            List<CounsellorTaskStatus> statuses
    );

    List<CounsellorTask> findAllByOrderByCreatedAtDesc();

    Optional<CounsellorTask> findFirstByStudent_IdOrderByCreatedAtDesc(Long studentId);
}
