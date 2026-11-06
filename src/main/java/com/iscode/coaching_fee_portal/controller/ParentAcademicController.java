package com.iscode.coaching_fee_portal.controller;

import com.iscode.coaching_fee_portal.dto.ParentAcademicResponse;
import com.iscode.coaching_fee_portal.service.ParentAcademicService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parent/academic")
public class ParentAcademicController {

    private final ParentAcademicService parentAcademicService;

    public ParentAcademicController(
            ParentAcademicService parentAcademicService
    ) {
        this.parentAcademicService = parentAcademicService;
    }

    @GetMapping
    public ResponseEntity<ParentAcademicResponse> getAcademicRecords(
            Authentication authentication
    ) {
        Long parentId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                parentAcademicService.getAcademicRecords(parentId)
        );
    }
}