
package com.iscode.coaching_fee_portal.controller;

import com.iscode.coaching_fee_portal.dto.parent.ParentDashboardResponse;
import com.iscode.coaching_fee_portal.service.ParentDashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parent")
public class ParentDashboardController {

    private final ParentDashboardService parentDashboardService;

    public ParentDashboardController(
            ParentDashboardService parentDashboardService) {
        this.parentDashboardService = parentDashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ParentDashboardResponse> getDashboard() {

        Long parentId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        ParentDashboardResponse response =
                parentDashboardService.getDashboard(parentId);

        return ResponseEntity.ok(response);
    }
}
