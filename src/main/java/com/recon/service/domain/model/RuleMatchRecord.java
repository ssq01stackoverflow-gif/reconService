package com.recon.service.domain.model;

import java.time.Instant;

public class RuleMatchRecord {

    private Long id;
    private String eventKey;
    private String ruleCode;
    private String bizType;
    private String bizKey;
    private Instant createdAt;

    public RuleMatchRecord() {
    }

    public RuleMatchRecord(String eventKey, String ruleCode, String bizType, String bizKey) {
        this.eventKey = eventKey;
        this.ruleCode = ruleCode;
        this.bizType = bizType;
        this.bizKey = bizKey;
        this.createdAt = Instant.now();
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
