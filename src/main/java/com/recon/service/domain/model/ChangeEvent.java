package com.recon.service.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ChangeEvent {

    private Long id;
    private String eventKey;
    private String destination;
    private String databaseName;
    private String tableName;
    private ChangeEventType eventType;
    private String primaryKey;
    private Map<String, Object> before;
    private Map<String, Object> after;
    private Set<String> changedFields;
    private String binlogFile;
    private String binlogPosition;
    private ChangeEventStatus status;
    private String rawMessage;
    private String errorMessage;
    private Instant eventTime;
    private Instant receiveTime;
    private Instant createdAt;
    private Instant updatedAt;

    public ChangeEvent() {
        this.before = new LinkedHashMap<String, Object>();
        this.after = new LinkedHashMap<String, Object>();
        this.changedFields = new LinkedHashSet<String>();
        this.status = ChangeEventStatus.INIT;
        this.receiveTime = Instant.now();
    }

    public static String buildEventKey(String destination, String databaseName, String tableName,
                                       String primaryKey, ChangeEventType eventType, String binlogPosition) {
        return destination + "|" + databaseName + "|" + tableName + "|" + primaryKey + "|" + eventType + "|" + binlogPosition;
    }

    public void markProcessing() {
        this.status = ChangeEventStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void markProcessed() {
        this.status = ChangeEventStatus.PROCESSED;
        this.errorMessage = null;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = ChangeEventStatus.FAILED;
        this.errorMessage = errorMessage;
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

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ChangeEventType getEventType() {
        return eventType;
    }

    public void setEventType(ChangeEventType eventType) {
        this.eventType = eventType;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Map<String, Object> getBefore() {
        return Collections.unmodifiableMap(before);
    }

    public void setBefore(Map<String, Object> before) {
        this.before = before == null ? new LinkedHashMap<String, Object>() : new LinkedHashMap<String, Object>(before);
    }

    public Map<String, Object> getAfter() {
        return Collections.unmodifiableMap(after);
    }

    public void setAfter(Map<String, Object> after) {
        this.after = after == null ? new LinkedHashMap<String, Object>() : new LinkedHashMap<String, Object>(after);
    }

    public Set<String> getChangedFields() {
        return Collections.unmodifiableSet(changedFields);
    }

    public void setChangedFields(Set<String> changedFields) {
        this.changedFields = changedFields == null ? new LinkedHashSet<String>() : new LinkedHashSet<String>(changedFields);
    }

    public String getBinlogFile() {
        return binlogFile;
    }

    public void setBinlogFile(String binlogFile) {
        this.binlogFile = binlogFile;
    }

    public String getBinlogPosition() {
        return binlogPosition;
    }

    public void setBinlogPosition(String binlogPosition) {
        this.binlogPosition = binlogPosition;
    }

    public ChangeEventStatus getStatus() {
        return status;
    }

    public void setStatus(ChangeEventStatus status) {
        this.status = status;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public void setEventTime(Instant eventTime) {
        this.eventTime = eventTime;
    }

    public Instant getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Instant receiveTime) {
        this.receiveTime = receiveTime;
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
