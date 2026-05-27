package com.recon.service.domain.service;

import com.recon.service.domain.model.ChangeEvent;
import com.recon.service.domain.model.ReconTask;
import com.recon.service.domain.model.ReconcilePolicy;

public class ReconcileContext {

    private final ChangeEvent event;
    private final ReconTask task;
    private final ReconcilePolicy policy;

    public ReconcileContext(ChangeEvent event, ReconTask task, ReconcilePolicy policy) {
        this.event = event;
        this.task = task;
        this.policy = policy;
    }

    public ChangeEvent getEvent() {
        return event;
    }

    public ReconTask getTask() {
        return task;
    }

    public ReconcilePolicy getPolicy() {
        return policy;
    }
}
