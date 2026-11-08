package com.iscode.coaching_fee_portal.dto;

import com.iscode.coaching_fee_portal.entity.CounsellorTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CounsellorTaskResponse {

    private Long taskId;

    private Long studentId;

    private String studentCode;

    private String studentName;

    private String batch;

    private Integer riskScore;

    private String riskLevel;

    private String riskReasons;

    private CounsellorTaskStatus status;

    private String falsePositiveReason;

    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;
}