package com.iscode.coaching_fee_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StudentRiskResponse {

    private Long studentId;
    private String studentCode;
    private String studentName;
    private String batch;

    private int riskScore;
    private String riskLevel;

    private List<String> riskFactors;

    private boolean attendanceDataAvailable;
    private boolean testDataAvailable;
    private boolean feeDataAvailable;
}