package com.iscode.coaching_fee_portal.repository;

import com.iscode.coaching_fee_portal.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByStudent_IdOrderByAttendanceDateDesc(Long studentId);

    long countByStudent_Id(Long studentId);

    long countByStudent_IdAndStatus(Long studentId, String status);
}