package com.recon.service.interfaces.dto;

import com.recon.service.domain.model.ReconTask;

import java.time.Instant;

public class TaskResponse {

    private Long id;
    private String eventKey;
    private String ruleCode;
    private String ruleName;
    private String bizType;
    private String bizKey;
    private String status;
    private int attemptCount;
    private int maxAttempts;
    private Instant nextRetryTime;
    private String lastCheckResult;
    private Instant createdAt;
    private Instant updatedAt;

    public static TaskResponse from(ReconTask task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setEventKey(task.getEventKey());
        response.setRuleCode(task.getRuleCode());
        response.setRuleName(task.getRuleName());
        response.setBizType(task.getBizType());
        response.setBizKey(task.getBizKey());
        response.setStatus(task.getStatus() == null ? null : task.getStatus().name());
        response.setAttemptCount(task.getAttemptCount());
        response.setMaxAttempts(task.getMaxAttempts());
        response.setNextRetryTime(task.getNextRetryTime());
        response.setLastCheckResult(task.getLastCheckResult());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getLastCheckResult() {
        return lastCheckResult;
    }

    public void setLastCheckResult(String lastCheckResult) {
        this.lastCheckResult = lastCheckResult;
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
}
