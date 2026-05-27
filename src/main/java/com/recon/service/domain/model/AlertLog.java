package com.recon.service.domain.model;

import java.time.Instant;

public class AlertLog {

    private Long id;
    private Long taskId;
    private String ruleCode;
    private String alertType;
    private String alertKey;
    private AlertLogStatus status;
    private String requestPayload;
    private String responsePayload;
    private String errorMessage;
    private int attemptCount;
    private Instant lastAttemptTime;
    private Instant nextRetryTime;
    private Instant createdAt;
    private Instant updatedAt;

    public AlertLog() {
        this.status = AlertLogStatus.PROCESSING;
        this.alertType = "LARK";
    }

    public static String buildAlertKey(Long taskId, String alertType) {
        return taskId + "|" + alertType;
    }

    public void markSuccess(String responsePayload) {
        this.status = AlertLogStatus.SUCCESS;
        this.responsePayload = responsePayload;
        this.errorMessage = null;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String errorMessage, Instant nextRetryTime) {
        this.status = AlertLogStatus.FAILED;
        this.errorMessage = errorMessage;
        this.nextRetryTime = nextRetryTime;
        this.updatedAt = Instant.now();
    }

    public void markProcessing() {
        this.status = AlertLogStatus.PROCESSING;
        this.attemptCount++;
        this.lastAttemptTime = Instant.now();
        this.updatedAt = this.lastAttemptTime;
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

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getAlertKey() {
        return alertKey;
    }

    public void setAlertKey(String alertKey) {
        this.alertKey = alertKey;
    }

    public AlertLogStatus getStatus() {
        return status;
    }

    public void setStatus(AlertLogStatus status) {
        this.status = status;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Instant getLastAttemptTime() {
        return lastAttemptTime;
    }

    public void setLastAttemptTime(Instant lastAttemptTime) {
        this.lastAttemptTime = lastAttemptTime;
    }

    public Instant getNextRetryTime() {
        return nextRetryTime;
    }

    public void setNextRetryTime(Instant nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
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
