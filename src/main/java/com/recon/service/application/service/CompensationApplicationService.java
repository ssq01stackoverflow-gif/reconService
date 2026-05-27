package com.recon.service.application.service;

import com.recon.service.config.ReconProperties;
import com.recon.service.domain.model.AlertLog;
import com.recon.service.domain.model.ChangeEvent;
import com.recon.service.domain.model.ReconTask;
import com.recon.service.domain.repository.AlertLogRepository;
import com.recon.service.domain.repository.ChangeEventRepository;
import com.recon.service.domain.repository.ReconTaskRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CompensationApplicationService {

    private final ChangeEventRepository changeEventRepository;
    private final ReconTaskRepository reconTaskRepository;
    private final AlertLogRepository alertLogRepository;
    private final AlertApplicationService alertApplicationService;
    private final ReconProperties reconProperties;

    public CompensationApplicationService(ChangeEventRepository changeEventRepository,
                                          ReconTaskRepository reconTaskRepository,
                                          AlertLogRepository alertLogRepository,
                                          AlertApplicationService alertApplicationService,
                                          ReconProperties reconProperties) {
        this.changeEventRepository = changeEventRepository;
        this.reconTaskRepository = reconTaskRepository;
        this.alertLogRepository = alertLogRepository;
        this.alertApplicationService = alertApplicationService;
        this.reconProperties = reconProperties;
    }

    public int recoverProcessingEvents() {
        Instant timeoutBefore = Instant.now().minusSeconds(reconProperties.getProcessingEventTimeoutSeconds());
        List<ChangeEvent> events = changeEventRepository.findProcessingBefore(timeoutBefore);
        for (ChangeEvent event : events) {
            event.markFailed("Processing timeout, waiting for reprocess");
            changeEventRepository.save(event);
        }
        return events.size();
    }

    public int recoverRunningTasks() {
        Instant timeoutBefore = Instant.now().minusSeconds(reconProperties.getRunningTaskTimeoutSeconds());
        List<ReconTask> tasks = reconTaskRepository.findRunningBefore(timeoutBefore);
        for (ReconTask task : tasks) {
            task.restorePreviousStatus("Running timeout recovered by compensation");
            reconTaskRepository.save(task);
        }
        return tasks.size();
    }

    public int compensateAlerts() {
        int count = 0;
        List<AlertLog> failedLogs = alertLogRepository.findFailedReadyToRetry(Instant.now(), reconProperties.getTaskScanLimit());
        for (AlertLog alertLog : failedLogs) {
            reconTaskRepository.findById(alertLog.getTaskId()).ifPresent(alertApplicationService::sendAlert);
            count++;
        }

        Instant timeoutBefore = Instant.now().minusSeconds(reconProperties.getAlertProcessingTimeoutSeconds());
        List<AlertLog> timeoutLogs = alertLogRepository.findProcessingBefore(timeoutBefore, reconProperties.getTaskScanLimit());
        for (AlertLog alertLog : timeoutLogs) {
            alertLog.markFailed("Alert processing timeout", Instant.now());
            alertLogRepository.save(alertLog);
            reconTaskRepository.findById(alertLog.getTaskId()).ifPresent(alertApplicationService::sendAlert);
            count++;
        }
        return count;
    }
}
