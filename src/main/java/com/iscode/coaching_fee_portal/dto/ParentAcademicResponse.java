package com.iscode.coaching_fee_portal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ParentAcademicResponse(
        List<ChildAcademicInfo> children
) {
    public record ChildAcademicInfo(
            Long studentId,
            String studentName,
            String studentCode,
            String batch,
            long totalClasses,
            long presentClasses,
            long absentClasses,
            long lateClasses,
            double attendancePercentage,
            List<AttendanceInfo> attendance,
            List<TestInfo> testResults
    ) {
    }

    public record AttendanceInfo(
            LocalDate date,
            com.iscode.coaching_fee_portal.entity.AttendanceStatus status
    ) {
    }

    public record TestInfo(
            String testName,
            String subject,
            BigDecimal marksObtained,
            BigDecimal maximumMarks,
            double percentage,
            LocalDate testDate
    ) {
    }
}