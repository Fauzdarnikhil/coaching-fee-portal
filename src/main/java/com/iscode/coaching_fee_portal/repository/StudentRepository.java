package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByCentreId(Long center_id);

    Optional<Student> findByStudentCode(String student_code);
}
