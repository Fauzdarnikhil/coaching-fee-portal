package com.iscode.coaching_fee_portal.dto.parent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class ParentDashboardResponse {
    private Long parentId;
    private String parentName;

    List<ChildInfo> children;
}
