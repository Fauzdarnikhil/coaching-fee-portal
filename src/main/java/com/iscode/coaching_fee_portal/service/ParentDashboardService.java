
package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.parent.ChildInfo;
import com.iscode.coaching_fee_portal.dto.parent.ParentDashboardResponse;
import com.iscode.coaching_fee_portal.entity.Parent;
import com.iscode.coaching_fee_portal.entity.ParentStudent;
import com.iscode.coaching_fee_portal.repository.ParentRepository;
import com.iscode.coaching_fee_portal.repository.ParentStudentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParentDashboardService {

    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;

    public ParentDashboardService(
            ParentRepository parentRepository,
            ParentStudentRepository parentStudentRepository) {

        this.parentRepository = parentRepository;
        this.parentStudentRepository = parentStudentRepository;
    }

    @Transactional(readOnly = true)
    public ParentDashboardResponse getDashboard(Long parentId) {

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Parent not found"));

        List<ParentStudent> relationships =
                parentStudentRepository.findByParentId(parentId);

        List<ChildInfo> children = relationships.stream()
                .map(relationship -> {

                    var student = relationship.getStudent();

                    return new ChildInfo(
                            student.getId(),
                            student.getName(),
                            student.getStudentCode(),
                            student.getBatch(),
                            relationship.getRelationship()
                    );
                })
                .toList();

        return new ParentDashboardResponse(
                parent.getId(),
                parent.getName(),
                children
        );
    }
}
