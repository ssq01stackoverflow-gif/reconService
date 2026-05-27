package com.recon.service.domain.model;

import java.time.Instant;

public class ReconExecutionLog {

    private Long id;
    private Long taskId;
    private String ruleCode;
    private ReconTaskStatus fromStatus;
    private ReconTaskStatus toStatus;
    private String result;
    private String message;
    private Instant startedAt;
    private Instant endedAt;

    public ReconExecutionLog() {
    }

    public ReconExecutionLog(Long taskId, String ruleCode, ReconTaskStatus fromStatus, ReconTaskStatus toStatus,
                             String result, String message, Instant startedAt, Instant endedAt) {
        this.taskId = taskId;
        this.ruleCode = ruleCode;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.result = result;
        this.message = message;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public ReconTaskStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(ReconTaskStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public ReconTaskStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ReconTaskStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }
}
