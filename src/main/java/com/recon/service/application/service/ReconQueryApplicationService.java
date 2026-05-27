package com.recon.service.application.service;

import com.recon.service.domain.model.AlertLog;
import com.recon.service.domain.model.ManualActionLog;
import com.recon.service.domain.model.ReconExecutionLog;
import com.recon.service.domain.model.ReconTask;
import com.recon.service.domain.model.ReconTaskStatus;
import com.recon.service.domain.repository.AlertLogRepository;
import com.recon.service.domain.repository.ManualActionLogRepository;
import com.recon.service.domain.repository.ReconExecutionLogRepository;
import com.recon.service.domain.repository.ReconTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReconQueryApplicationService {

    private final ReconTaskRepository reconTaskRepository;
    private final ReconExecutionLogRepository executionLogRepository;
    private final AlertLogRepository alertLogRepository;
    private final ManualActionLogRepository manualActionLogRepository;

    public ReconQueryApplicationService(ReconTaskRepository reconTaskRepository,
                                        ReconExecutionLogRepository executionLogRepository,
                                        AlertLogRepository alertLogRepository,
                                        ManualActionLogRepository manualActionLogRepository) {
        this.reconTaskRepository = reconTaskRepository;
        this.executionLogRepository = executionLogRepository;
        this.alertLogRepository = alertLogRepository;
        this.manualActionLogRepository = manualActionLogRepository;
    }

    public ReconTask getTask(Long taskId) {
        return reconTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Recon task not found: " + taskId));
    }

    public List<ReconTask> findTasksByStatus(ReconTaskStatus status) {
        return reconTaskRepository.findByStatus(status);
    }

    public List<ReconExecutionLog> findExecutionLogs(Long taskId) {
        return executionLogRepository.findByTaskId(taskId);
    }

    public List<AlertLog> findAlertLogs(Long taskId) {
        return alertLogRepository.findByTaskId(taskId);
    }

    public List<ManualActionLog> findManualActionLogs(Long taskId) {
        return manualActionLogRepository.findByTaskId(taskId);
    }
}
