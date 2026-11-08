package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.ParentAcademicResponse;
import com.iscode.coaching_fee_portal.entity.AttendanceStatus;
import com.iscode.coaching_fee_portal.entity.ParentStudent;
import com.iscode.coaching_fee_portal.entity.Student;
import com.iscode.coaching_fee_portal.repository.ParentStudentRepository;
import com.iscode.coaching_fee_portal.repository.AttendanceRecordRepository;
import com.iscode.coaching_fee_portal.repository.TestResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParentAcademicService {

    private final ParentStudentRepository parentStudentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TestResultRepository testResultRepository;

    public ParentAcademicService(
            ParentStudentRepository parentStudentRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            TestResultRepository testResultRepository
    ) {
        this.parentStudentRepository = parentStudentRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.testResultRepository = testResultRepository;
    }

    @Transactional(readOnly = true)
    public ParentAcademicResponse getAcademicRecords(Long parentId) {

        List<ParentStudent> mappings =
                parentStudentRepository.findByParentId(parentId);

        List<ParentAcademicResponse.ChildAcademicInfo> children =
                mappings.stream().map(mapping -> {

                    Student student = mapping.getStudent();
                    Long studentId = student.getId();

                    long totalClasses =
                            attendanceRecordRepository.countByStudent_Id(studentId);

                    long presentClasses =
                            attendanceRecordRepository.countByStudent_IdAndStatus(
                                    studentId, AttendanceStatus.PRESENT
                            );

                    long absentClasses =
                            attendanceRecordRepository.countByStudent_IdAndStatus(
                                    studentId, AttendanceStatus.ABSENT
                            );

                    long lateClasses =
                            attendanceRecordRepository.countByStudent_IdAndStatus(
                                    studentId, AttendanceStatus.LATE
                            );

                    double attendancePercentage = totalClasses == 0
                            ? 0.0
                            : Math.round((presentClasses + lateClasses) * 10000.0 / totalClasses) / 100.0;

                    List<ParentAcademicResponse.AttendanceInfo> attendance =
                            attendanceRecordRepository
                                    .findByStudent_IdOrderByClassDateDesc(studentId)
                                    .stream()
                                    .map(record -> new ParentAcademicResponse.AttendanceInfo(
                                            record.getClassDate(),
                                            record.getStatus()
                                    ))
                                    .toList();

                    List<ParentAcademicResponse.TestInfo> testResults =
                            testResultRepository
                                    .findByStudent_IdOrderByTestDateDesc(studentId)
                                    .stream()
                                    .map(result -> new ParentAcademicResponse.TestInfo(
                                            result.getTestName(),
                                            result.getSubject(),
                                            result.getMarksObtained(),
                                            result.getMaximumMarks(),
                                            result.getMaximumMarks().doubleValue() == 0
                                                    ? 0.0
                                                    : Math.round(
                                                    result.getMarksObtained().doubleValue() * 10000.0
                                                            / result.getMaximumMarks().doubleValue()
                                            ) / 100.0,
                                            result.getTestDate()
                                    ))
                                    .toList();

                    return new ParentAcademicResponse.ChildAcademicInfo(
                            studentId,
                            student.getName(),
                            student.getStudentCode(),
                            student.getBatch(),
                            totalClasses,
                            presentClasses,
                            absentClasses,
                            lateClasses,
                            attendancePercentage,
                            attendance,
                            testResults
                    );

                }).toList();

        return new ParentAcademicResponse(children);
    }
}