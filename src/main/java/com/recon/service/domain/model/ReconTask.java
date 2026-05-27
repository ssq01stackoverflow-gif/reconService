package com.recon.service.domain.model;

import java.time.Instant;

public class ReconTask {

    private Long id;
    private String eventKey;
    private String ruleCode;
    private String ruleName;
    private String bizType;
    private String bizKey;
    private ReconTaskStatus status;
    private ReconTaskStatus previousStatus;
    private int attemptCount;
    private int maxAttempts;
    private Instant nextRetryTime;
    private Instant lastRunTime;
    private String lastErrorCode;
    private String lastErrorMessage;
    private String lastCheckResult;
    private String lockedBy;
    private Instant lockedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private long version;

    public ReconTask() {
        this.status = ReconTaskStatus.INIT;
        this.attemptCount = 0;
        this.maxAttempts = ReconcilePolicy.defaultPolicy().getMaxAttempts();
    }

    public boolean canRun(Instant now) {
        if (status == ReconTaskStatus.INIT) {
            return true;
        }
        return status == ReconTaskStatus.RETRY_WAIT && (nextRetryTime == null || !nextRetryTime.isAfter(now));
    }

    public void markRunning(String worker) {
        this.previousStatus = this.status;
        this.status = ReconTaskStatus.RUNNING;
        this.lockedBy = worker;
        this.lockedAt = Instant.now();
        this.lastRunTime = this.lockedAt;
    }

    public void markSuccess(String message) {
        this.status = ReconTaskStatus.SUCCESS;
        this.lastCheckResult = message;
        clearLock();
    }

    public void markResolved(String reason) {
        this.status = ReconTaskStatus.RESOLVED;
        this.lastCheckResult = reason;
        clearLock();
    }

    public void markRetryWait(Instant nextRetryTime, String reason) {
        this.status = ReconTaskStatus.RETRY_WAIT;
        this.nextRetryTime = nextRetryTime;
        this.lastCheckResult = reason;
        clearLock();
    }

    public void markFailedFinal(String reason) {
        this.status = ReconTaskStatus.FAILED_FINAL;
        this.lastCheckResult = reason;
        clearLock();
    }

    public void markAlerting() {
        this.status = ReconTaskStatus.ALERTING;
        clearLock();
    }

    public void markPaused(String reason) {
        this.status = ReconTaskStatus.PAUSED;
        this.lastCheckResult = reason;
        clearLock();
    }

    public void restorePreviousStatus(String reason) {
        if (previousStatus != null) {
            this.status = previousStatus;
        }
        this.lastErrorMessage = reason;
        clearLock();
    }

    public void incrementAttempt() {
        this.attemptCount++;
    }

    public void clearLock() {
        this.lockedBy = null;
        this.lockedAt = null;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getBizKey() {
        return bizKey;
    }

    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
    }

    public ReconTaskStatus getStatus() {
        return status;
    }

    public void setStatus(ReconTaskStatus status) {
        this.status = status;
    }

    public ReconTaskStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(ReconTaskStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Instant getNextRetryTime() {
        return nextRetryTime;
    }

    public void setNextRetryTime(Instant nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

    public Instant getLastRunTime() {
        return lastRunTime;
    }

    public void setLastRunTime(Instant lastRunTime) {
        this.lastRunTime = lastRunTime;
    }

    public String getLastErrorCode() {
        return lastErrorCode;
    }

    public void setLastErrorCode(String lastErrorCode) {
        this.lastErrorCode = lastErrorCode;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }

    public String getLastCheckResult() {
        return lastCheckResult;
    }

    public void setLastCheckResult(String lastCheckResult) {
        this.lastCheckResult = lastCheckResult;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Instant lockedAt) {
        this.lockedAt = lockedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
