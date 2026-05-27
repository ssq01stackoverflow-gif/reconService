package com.recon.service.domain.model;

import java.time.Instant;

public class ManualActionLog {

    private Long id;
    private Long taskId;
    private ManualActionType action;
    private String operator;
    private String reason;
    private String remark;
    private ReconTaskStatus fromStatus;
    private ReconTaskStatus toStatus;
    private Instant createdAt;

    public ManualActionLog() {
    }

    public ManualActionLog(Long taskId, ManualActionType action, String operator, String reason, String remark,
                           ReconTaskStatus fromStatus, ReconTaskStatus toStatus) {
        this.taskId = taskId;
        this.action = action;
        this.operator = operator;
        this.reason = reason;
        this.remark = remark;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.createdAt = Instant.now();
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

    public ManualActionType getAction() {
        return action;
    }

    public void setAction(ManualActionType action) {
        this.action = action;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
