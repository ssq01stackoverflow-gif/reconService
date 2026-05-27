package com.recon.service.application.service;

import com.recon.service.domain.model.ManualActionLog;
import com.recon.service.domain.model.ManualActionType;
import com.recon.service.domain.model.ReconTask;
import com.recon.service.domain.model.ReconTaskStatus;
import com.recon.service.domain.repository.ManualActionLogRepository;
import com.recon.service.domain.repository.ReconTaskRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ManualTaskApplicationService {

    private final ReconTaskRepository reconTaskRepository;
    private final ManualActionLogRepository manualActionLogRepository;

    public ManualTaskApplicationService(ReconTaskRepository reconTaskRepository,
                                        ManualActionLogRepository manualActionLogRepository) {
        this.reconTaskRepository = reconTaskRepository;
        this.manualActionLogRepository = manualActionLogRepository;
    }

    public ReconTask markNeedHandle(Long taskId, String operator, String reason, String remark) {
        ReconTask task = getTask(taskId);
        ensureStatus(task, ReconTaskStatus.ALERTING);
        ReconTaskStatus fromStatus = task.getStatus();
        task.markPaused(reason);
        reconTaskRepository.save(task);
        saveLog(task, ManualActionType.MARK_NEED_HANDLE, operator, reason, remark, fromStatus);
        return task;
    }

    public ReconTask markNoNeedHandle(Long taskId, String operator, String reason, String remark) {
        ReconTask task = getTask(taskId);
        if (task.getStatus() != ReconTaskStatus.ALERTING && task.getStatus() != ReconTaskStatus.PAUSED) {
            throw new IllegalStateException("Task status does not allow resolve: " + task.getStatus());
        }
        ReconTaskStatus fromStatus = task.getStatus();
        task.markResolved(reason);
        reconTaskRepository.save(task);
        saveLog(task, ManualActionType.MARK_NO_NEED_HANDLE, operator, reason, remark, fromStatus);
        return task;
    }

    public ReconTask retry(Long taskId, String operator, String reason, String remark) {
        ReconTask task = getTask(taskId);
        ensureStatus(task, ReconTaskStatus.PAUSED);
        ReconTaskStatus fromStatus = task.getStatus();
        task.markRetryWait(Instant.now(), reason);
        reconTaskRepository.save(task);
        saveLog(task, ManualActionType.RETRY, operator, reason, remark, fromStatus);
        return task;
    }

    public ManualActionLog addNote(Long taskId, String operator, String reason, String remark) {
        ReconTask task = getTask(taskId);
        return saveLog(task, ManualActionType.ADD_NOTE, operator, reason, remark, task.getStatus());
    }

    private ReconTask getTask(Long taskId) {
        return reconTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Recon task not found: " + taskId));
    }

    private void ensureStatus(ReconTask task, ReconTaskStatus expected) {
        if (task.getStatus() != expected) {
            throw new IllegalStateException("Expected task status " + expected + " but was " + task.getStatus());
        }
    }

    private ManualActionLog saveLog(ReconTask task, ManualActionType action, String operator, String reason,
                                    String remark, ReconTaskStatus fromStatus) {
        ManualActionLog log = new ManualActionLog(
                task.getId(),
                action,
                operator,
                reason,
                remark,
                fromStatus,
                task.getStatus());
        return manualActionLogRepository.save(log);
    }
}
