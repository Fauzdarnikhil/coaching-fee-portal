package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.InstalmentLedgerResponse;
import com.iscode.coaching_fee_portal.dto.parent.ChildInfo;
import com.iscode.coaching_fee_portal.dto.parent.FeePlanLedgerInfo;
import com.iscode.coaching_fee_portal.dto.parent.ParentDashboardResponse;
import com.iscode.coaching_fee_portal.entity.FeePlan;
import com.iscode.coaching_fee_portal.entity.Parent;
import com.iscode.coaching_fee_portal.entity.ParentStudent;
import com.iscode.coaching_fee_portal.entity.Student;
import com.iscode.coaching_fee_portal.repository.FeePlanRepository;
import com.iscode.coaching_fee_portal.repository.ParentRepository;
import com.iscode.coaching_fee_portal.repository.ParentStudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ParentDashboardService {

    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final FeePlanRepository feePlanRepository;
    private final FeeLedgerService feeLedgerService;

    public ParentDashboardService(
            ParentRepository parentRepository,
            ParentStudentRepository parentStudentRepository,
            FeePlanRepository feePlanRepository,
            FeeLedgerService feeLedgerService) {

        this.parentRepository = parentRepository;
        this.parentStudentRepository = parentStudentRepository;
        this.feePlanRepository = feePlanRepository;
        this.feeLedgerService = feeLedgerService;
    }

    @Transactional(readOnly = true)
    public ParentDashboardResponse getDashboard(Long parentId) {

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        List<ParentStudent> mappings =
                parentStudentRepository.findByParentId(parentId);

        List<ChildInfo> children = mappings.stream()
                .map(mapping -> {

                    Student student = mapping.getStudent();

                    List<FeePlan> feePlans =
                            feePlanRepository.findByStudentId(student.getId());

                    List<FeePlanLedgerInfo> feePlanInfos = feePlans.stream()
                            .map(feePlan -> {

                                List<InstalmentLedgerResponse> instalments =
                                        feeLedgerService.getFeePlanLedger(
                                                feePlan.getId()
                                        );

                                BigDecimal netFee = feePlan.getTotalFee()
                                        .subtract(feePlan.getDiscountAmount());

                                return new FeePlanLedgerInfo(
                                        feePlan.getId(),
                                        feePlan.getAcademicYear(),
                                        feePlan.getTotalFee(),
                                        feePlan.getDiscountAmount(),
                                        netFee,
                                        instalments
                                );
                            })
                            .toList();

                    return new ChildInfo(
                            student.getId(),
                            student.getName(),
                            student.getStudentCode(),
                            student.getBatch(),
                            mapping.getRelationship(),
                            feePlanInfos
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