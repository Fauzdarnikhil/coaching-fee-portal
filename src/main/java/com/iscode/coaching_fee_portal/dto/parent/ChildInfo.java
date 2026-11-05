package com.iscode.coaching_fee_portal.dto.parent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class ChildInfo {
    private Long studentId;
    private String studentName;
    private String studentCode;
    private String batchName;
    private String relationship;

}
