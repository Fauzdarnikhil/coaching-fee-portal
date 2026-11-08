
package com.iscode.coaching_fee_portal.controller;

import com.iscode.coaching_fee_portal.dto.StudentRiskResponse;
import com.iscode.coaching_fee_portal.repository.StudentRepository;
import com.iscode.coaching_fee_portal.service.StudentRiskService;
import com.iscode.coaching_fee_portal.service.CounsellorTaskService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class StudentRiskController {

    private final StudentRiskService studentRiskService;
    private final StudentRepository studentRepository;
    private final CounsellorTaskService counsellorTaskService;

    public StudentRiskController(
            StudentRiskService studentRiskService,
            StudentRepository studentRepository,
            CounsellorTaskService counsellorTaskService
    ) {
        this.studentRiskService = studentRiskService;
        this.studentRepository = studentRepository;
        this.counsellorTaskService = counsellorTaskService;
    }

    @GetMapping("/student-risks")
    public List<StudentRiskResponse> getStudentRisks() {

        return studentRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(student -> {

                    StudentRiskResponse riskResponse =
                            studentRiskService.calculateRisk(student);

                    counsellorTaskService.createTaskIfRequired(
                            student,
                            riskResponse
                    );

                    return riskResponse;
                })
                .toList();
    }
}
