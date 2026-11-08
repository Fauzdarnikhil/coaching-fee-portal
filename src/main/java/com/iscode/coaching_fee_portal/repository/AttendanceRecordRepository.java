package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.AttendanceRecord;
import com.iscode.coaching_fee_portal.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByStudent_IdOrderByClassDateDesc(Long studentId);

    long countByStudent_Id(Long studentId);

    long countByStudent_IdAndStatus(Long studentId, AttendanceStatus status);
}