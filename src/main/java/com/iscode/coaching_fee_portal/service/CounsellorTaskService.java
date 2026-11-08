package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.dto.CounsellorTaskResponse;
import com.iscode.coaching_fee_portal.dto.StudentRiskResponse;
import com.iscode.coaching_fee_portal.entity.CounsellorTask;
import com.iscode.coaching_fee_portal.entity.CounsellorTaskStatus;
import com.iscode.coaching_fee_portal.entity.Student;
import com.iscode.coaching_fee_portal.repository.CounsellorTaskRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CounsellorTaskService {

    private final CounsellorTaskRepository counsellorTaskRepository;

    public CounsellorTaskService(
            CounsellorTaskRepository counsellorTaskRepository
    ) {
        this.counsellorTaskRepository = counsellorTaskRepository;
    }

    @Transactional(readOnly = true)
    public List<CounsellorTaskResponse> getAllTasks() {

        return counsellorTaskRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(task -> new CounsellorTaskResponse(
                        task.getId(),
                        task.getStudent().getId(),
                        task.getStudent().getStudentCode(),
                        task.getStudent().getName(),
                        task.getStudent().getBatch(),
                        task.getRiskScore(),
                        task.getRiskLevel(),
                        task.getRiskReasons(),
                        task.getStatus(),
                        task.getFalsePositiveReason(),
                        task.getCreatedAt(),
                        task.getReviewedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsContacted(Long taskId) {

        CounsellorTask task = counsellorTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Counsellor task not found"
                ));

        if (task.getStatus() == CounsellorTaskStatus.FALSE_POSITIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "False positive tasks cannot be marked as contacted"
            );
        }

        task.setStatus(CounsellorTaskStatus.CONTACTED);
        task.setReviewedAt(LocalDateTime.now());

        counsellorTaskRepository.save(task);
    }

    @Transactional
    public void createTaskIfRequired(Student student, StudentRiskResponse riskResponse) {

        String riskLevel = riskResponse.getRiskLevel();

        if (!riskLevel.equals("MEDIUM") && !riskLevel.equals("HIGH")) {
            return;
        }

        CounsellorTask latestTask = counsellorTaskRepository
                .findFirstByStudent_IdOrderByCreatedAtDesc(student.getId())
                .orElse(null);

        if (latestTask != null) {

            if (latestTask.getStatus() == CounsellorTaskStatus.OPEN
                    || latestTask.getStatus() == CounsellorTaskStatus.CONTACTED) {
                return;
            }

            if (latestTask.getStatus() == CounsellorTaskStatus.FALSE_POSITIVE
                    && latestTask.getRiskScore().equals(riskResponse.getRiskScore())
                    && latestTask.getRiskLevel().equals(riskResponse.getRiskLevel())
                    && latestTask.getRiskReasons().equals(
                    String.join("; ", riskResponse.getRiskFactors())
            )) {
                return;
            }
        }

        String riskReasons = String.join("; ", riskResponse.getRiskFactors());

        CounsellorTask task = new CounsellorTask(
                student,
                riskResponse.getRiskScore(),
                riskLevel,
                riskReasons
        );

        counsellorTaskRepository.save(task);
    }

    @Transactional
    public void markAsFalsePositive(Long taskId, String reason) {

        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A reason is required to mark a task as false positive"
            );
        }

        CounsellorTask task = counsellorTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Counsellor task not found"
                ));

        if (task.getStatus() == CounsellorTaskStatus.FALSE_POSITIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Task is already marked as false positive"
            );
        }

        task.setStatus(CounsellorTaskStatus.FALSE_POSITIVE);
        task.setFalsePositiveReason(reason.trim());
        task.setReviewedAt(LocalDateTime.now());

        counsellorTaskRepository.save(task);
    }
}
