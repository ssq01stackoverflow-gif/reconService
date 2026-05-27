package com.recon.service.application.service;

import com.recon.service.domain.model.ChangeEvent;
import com.recon.service.domain.model.ReconTask;

import java.util.Collections;
import java.util.List;

public class ChangeEventProcessResult {

    private final String result;
    private final ChangeEvent event;
    private final List<ReconTask> createdTasks;

    private ChangeEventProcessResult(String result, ChangeEvent event, List<ReconTask> createdTasks) {
        this.result = result;
        this.event = event;
        this.createdTasks = createdTasks;
    }

    public static ChangeEventProcessResult processed(String result, ChangeEvent event, List<ReconTask> createdTasks) {
        return new ChangeEventProcessResult(result, event, createdTasks);
    }

    public static ChangeEventProcessResult duplicateProcessed(ChangeEvent event) {
        return new ChangeEventProcessResult("DUPLICATE_PROCESSED", event, Collections.<ReconTask>emptyList());
    }

    public static ChangeEventProcessResult duplicateSkipped(ChangeEvent event) {
        return new ChangeEventProcessResult("DUPLICATE_SKIPPED", event, Collections.<ReconTask>emptyList());
    }

    public static ChangeEventProcessResult duplicateReprocessed(ChangeEvent event) {
        return new ChangeEventProcessResult("DUPLICATE_REPROCESSED", event, Collections.<ReconTask>emptyList());
    }

    public String getResult() {
        return result;
    }

    public ChangeEvent getEvent() {
        return event;
    }

    public List<ReconTask> getCreatedTasks() {
        return createdTasks;
    }
}
