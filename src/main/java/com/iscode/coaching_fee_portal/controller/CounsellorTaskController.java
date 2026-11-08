package com.iscode.coaching_fee_portal.controller;

import com.iscode.coaching_fee_portal.dto.CounsellorTaskResponse;
import com.iscode.coaching_fee_portal.dto.FalsePositiveRequest;
import com.iscode.coaching_fee_portal.service.CounsellorTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts/counsellor-tasks")
public class CounsellorTaskController {

    private final CounsellorTaskService counsellorTaskService;

    public CounsellorTaskController(CounsellorTaskService counsellorTaskService) {
        this.counsellorTaskService = counsellorTaskService;
    }

    @GetMapping
    public List<CounsellorTaskResponse> getAllTasks() {
        return counsellorTaskService.getAllTasks();
    }

    @PatchMapping("/{taskId}/contacted")
    public void markAsContacted(@PathVariable Long taskId) {
        counsellorTaskService.markAsContacted(taskId);
    }

    @PatchMapping("/{taskId}/false-positive")
    public void markAsFalsePositive(
            @PathVariable Long taskId,
            @RequestBody FalsePositiveRequest request
    ) {
        counsellorTaskService.markAsFalsePositive(
                taskId,
                request.getReason()
        );
    }
}