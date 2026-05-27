package com.recon.service.application.service;

import com.recon.service.config.ReconProperties;
import com.recon.service.domain.model.AlertLog;
import com.recon.service.domain.model.AlertLogStatus;
import com.recon.service.domain.model.ReconTask;
import com.recon.service.domain.model.ReconTaskStatus;
import com.recon.service.domain.repository.AlertLogRepository;
import com.recon.service.domain.repository.ReconTaskRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AlertApplicationService {

    private final ReconTaskRepository reconTaskRepository;
    private final AlertLogRepository alertLogRepository;
    private final AlertClient alertClient;
    private final ReconProperties reconProperties;

    public AlertApplicationService(ReconTaskRepository reconTaskRepository,
                                   AlertLogRepository alertLogRepository,
                                   AlertClient alertClient,
                                   ReconProperties reconProperties) {
        this.reconTaskRepository = reconTaskRepository;
        this.alertLogRepository = alertLogRepository;
        this.alertClient = alertClient;
        this.reconProperties = reconProperties;
    }

    public int scanAndAlert() {
        List<ReconTask> tasks = reconTaskRepository.findByStatus(ReconTaskStatus.FAILED_FINAL);
        int count = 0;
        for (ReconTask task : tasks) {
            sendAlert(task);
            count++;
        }
        return count;
    }

    public AlertLog sendAlert(ReconTask task) {
        String alertKey = AlertLog.buildAlertKey(task.getId(), "LARK");
        Optional<AlertLog> existing = alertLogRepository.findByAlertKey(alertKey);
        if (existing.isPresent() && existing.get().getStatus() == AlertLogStatus.SUCCESS) {
            task.markAlerting();
            reconTaskRepository.save(task);
            return existing.get();
        }

        AlertLog alertLog = existing.orElseGet(() -> {
            AlertLog log = new AlertLog();
            log.setTaskId(task.getId());
            log.setRuleCode(task.getRuleCode());
            log.setAlertType("LARK");
            log.setAlertKey(alertKey);
            log.setRequestPayload(buildAlertMessage(task));
            return alertLogRepository.insertIfAbsent(log).orElse(log);
        });

        if (alertLog.getAttemptCount() >= reconProperties.getAlertMaxAttempts()) {
            return alertLog;
        }

        task.markAlerting();
        reconTaskRepository.save(task);
        alertLog.markProcessing();
        alertLogRepository.save(alertLog);

        AlertSendResult sendResult = alertClient.send(task, alertLog.getRequestPayload());
        if (sendResult.isSuccess()) {
            alertLog.markSuccess(sendResult.getResponsePayload());
        } else {
            int delaySeconds = reconProperties.alertRetryDelaySecondsForAttempt(alertLog.getAttemptCount());
            alertLog.markFailed(sendResult.getErrorMessage(), Instant.now().plusSeconds(delaySeconds));
        }
        alertLogRepository.save(alertLog);
        return alertLog;
    }

    private String buildAlertMessage(ReconTask task) {
        StringBuilder builder = new StringBuilder();
        builder.append("规则编码：").append(task.getRuleCode()).append('\n');
        builder.append("规则名称：").append(task.getRuleName()).append('\n');
        builder.append("任务ID：").append(task.getId()).append('\n');
        builder.append("业务主键：").append(task.getBizKey()).append('\n');
        builder.append("失败原因：").append(task.getLastCheckResult()).append('\n');
        builder.append("首次触发时间：").append(task.getCreatedAt()).append('\n');
        builder.append("最后重试时间：").append(task.getLastRunTime()).append('\n');
        return builder.toString();
    }
}
