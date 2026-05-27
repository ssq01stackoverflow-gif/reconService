package com.recon.service.application.service;

import com.recon.service.domain.model.ChangeEvent;
import com.recon.service.domain.model.CheckResult;
import com.recon.service.domain.model.CheckResultStatus;
import com.recon.service.domain.model.PreCheckResult;
import com.recon.service.domain.model.ReconExecutionLog;
import com.recon.service.domain.model.ReconTask;
import com.recon.service.domain.model.ReconTaskStatus;
import com.recon.service.domain.model.ReconcilePolicy;
import com.recon.service.domain.repository.ChangeEventRepository;
import com.recon.service.domain.repository.ReconExecutionLogRepository;
import com.recon.service.domain.repository.ReconTaskRepository;
import com.recon.service.domain.service.ReconcileContext;
import com.recon.service.domain.service.ReconcileRule;
import com.recon.service.domain.service.ReconcileRuleRegistry;
import com.recon.service.domain.service.RetryPolicyCalculator;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ReconTaskExecutionService {

    private final ReconTaskRepository reconTaskRepository;
    private final ChangeEventRepository changeEventRepository;
    private final ReconExecutionLogRepository executionLogRepository;
    private final ReconcileRuleRegistry ruleRegistry;
    private final RetryPolicyCalculator retryPolicyCalculator = new RetryPolicyCalculator();

    public ReconTaskExecutionService(ReconTaskRepository reconTaskRepository,
                                     ChangeEventRepository changeEventRepository,
                                     ReconExecutionLogRepository executionLogRepository,
                                     ReconcileRuleRegistry ruleRegistry) {
        this.reconTaskRepository = reconTaskRepository;
        this.changeEventRepository = changeEventRepository;
        this.executionLogRepository = executionLogRepository;
        this.ruleRegistry = ruleRegistry;
    }

    public boolean executeClaimedTask(Long taskId) {
        ReconTask task = reconTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Recon task not found: " + taskId));
        if (task.getStatus() != ReconTaskStatus.RUNNING) {
            return false;
        }
        ChangeEvent event = changeEventRepository.findByEventKey(task.getEventKey())
                .orElseThrow(() -> new IllegalStateException("Change event not found: " + task.getEventKey()));
        ReconcileRule<?, ?> rule = ruleRegistry.getRequired(task.getRuleCode());
        executeRule(rule, event, task);
        return true;
    }

    @SuppressWarnings("unchecked")
    private <L, R> void executeRule(ReconcileRule<L, R> rule, ChangeEvent event, ReconTask task) {
        Instant startedAt = Instant.now();
        ReconTaskStatus fromStatus = task.getPreviousStatus();
        ReconcilePolicy policy = rule.defaultPolicy();
        try {
            ReconcileContext ctx = new ReconcileContext(event, task, policy);
            L left = rule.loadLeft(ctx);
            PreCheckResult preCheckResult = rule.preCheck(left, ctx);
            if (!preCheckResult.isPassed()) {
                task.markResolved(preCheckResult.getReason());
                reconTaskRepository.save(task);
                saveLog(task, fromStatus, "PRE_CHECK_SKIPPED", preCheckResult.getReason(), startedAt);
                return;
            }

            R right = rule.loadRight(left, ctx);
            CheckResult checkResult = rule.check(left, right, ctx);
            handleCheckResult(task, policy, checkResult, startedAt, fromStatus);
        } catch (Exception ex) {
            task.restorePreviousStatus(ex.getMessage());
            task.setLastErrorCode("EXECUTION_ERROR");
            reconTaskRepository.save(task);
            saveLog(task, fromStatus, "ERROR", ex.getMessage(), startedAt);
        }
    }

    private void handleCheckResult(ReconTask task, ReconcilePolicy policy, CheckResult checkResult,
                                   Instant startedAt, ReconTaskStatus fromStatus) {
        if (checkResult.getStatus() == CheckResultStatus.SUCCESS) {
            task.markSuccess(checkResult.getMessage());
            reconTaskRepository.save(task);
            saveLog(task, fromStatus, "SUCCESS", checkResult.getMessage(), startedAt);
            return;
        }

        if (checkResult.getStatus() == CheckResultStatus.NOT_READY) {
            task.restorePreviousStatus(checkResult.getMessage());
            reconTaskRepository.save(task);
            saveLog(task, fromStatus, "NOT_READY", checkResult.getMessage(), startedAt);
            return;
        }

        task.incrementAttempt();
        if (task.getAttemptCount() >= task.getMaxAttempts()) {
            task.markFailedFinal(checkResult.getMessage());
            reconTaskRepository.save(task);
            saveLog(task, fromStatus, "FAILED_FINAL", checkResult.getMessage(), startedAt);
            return;
        }

        Instant nextRetryTime = retryPolicyCalculator.nextRetryTime(Instant.now(), task.getAttemptCount(), policy);
        task.markRetryWait(nextRetryTime, checkResult.getMessage());
        reconTaskRepository.save(task);
        saveLog(task, fromStatus, "RETRY_WAIT", checkResult.getMessage(), startedAt);
    }

    private void saveLog(ReconTask task, ReconTaskStatus fromStatus, String result, String message, Instant startedAt) {
        ReconExecutionLog log = new ReconExecutionLog(
                task.getId(),
                task.getRuleCode(),
                fromStatus,
                task.getStatus(),
                result,
                message,
                startedAt,
                Instant.now());
        executionLogRepository.save(log);
    }
}
