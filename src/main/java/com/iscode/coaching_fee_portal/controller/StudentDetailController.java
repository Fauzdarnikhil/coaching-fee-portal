
package com.iscode.coaching_fee_portal.controller;

import com.iscode.coaching_fee_portal.dto.accounts.StudentDetailResponse;
import com.iscode.coaching_fee_portal.service.StudentDetailService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts/students")
public class StudentDetailController {

    private final StudentDetailService studentDetailService;

    public StudentDetailController(
            StudentDetailService studentDetailService
    ) {
        this.studentDetailService = studentDetailService;
    }

    @GetMapping("/{studentId}")
    public StudentDetailResponse getStudentDetails(
            @PathVariable Long studentId
    ) {
        return studentDetailService.getStudentDetails(studentId);
    }
}
