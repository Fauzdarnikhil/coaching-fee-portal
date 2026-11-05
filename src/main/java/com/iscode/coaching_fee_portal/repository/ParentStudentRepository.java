package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.ParentStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {

    List<ParentStudent> findByParentId(Long parentId);

    List<ParentStudent> findByStudentId(Long studentId);

    Optional<ParentStudent> findByParentIdAndStudentId(Long parentId, Long studentId);

    boolean existsByParentIdAndStudentId(Long parentId, Long studentId);
}
