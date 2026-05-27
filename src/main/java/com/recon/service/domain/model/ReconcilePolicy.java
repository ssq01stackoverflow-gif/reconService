package com.recon.service.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReconcilePolicy {

    private int maxAttempts;
    private List<Integer> retryIntervalsSeconds;
    private boolean alertEnabled;
    private String alertTemplateCode;

    public ReconcilePolicy() {
        this(3, defaultRetryIntervals(), true, "DEFAULT");
    }

    public ReconcilePolicy(int maxAttempts, List<Integer> retryIntervalsSeconds, boolean alertEnabled, String alertTemplateCode) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be greater than 0");
        }
        this.maxAttempts = maxAttempts;
        setRetryIntervalsSeconds(retryIntervalsSeconds);
        this.alertEnabled = alertEnabled;
        this.alertTemplateCode = alertTemplateCode;
    }

    public static ReconcilePolicy defaultPolicy() {
        return new ReconcilePolicy();
    }

    private static List<Integer> defaultRetryIntervals() {
        List<Integer> intervals = new ArrayList<Integer>();
        intervals.add(30);
        intervals.add(60);
        intervals.add(90);
        return intervals;
    }

    public int retryDelaySecondsForAttempt(int attemptCount) {
        if (retryIntervalsSeconds.isEmpty()) {
            return 0;
        }
        int index = Math.max(0, attemptCount - 1);
        if (index >= retryIntervalsSeconds.size()) {
            index = retryIntervalsSeconds.size() - 1;
        }
        return retryIntervalsSeconds.get(index);
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be greater than 0");
        }
        this.maxAttempts = maxAttempts;
    }

    public List<Integer> getRetryIntervalsSeconds() {
        return Collections.unmodifiableList(retryIntervalsSeconds);
    }

    public void setRetryIntervalsSeconds(List<Integer> retryIntervalsSeconds) {
        if (retryIntervalsSeconds == null || retryIntervalsSeconds.isEmpty()) {
            this.retryIntervalsSeconds = defaultRetryIntervals();
            return;
        }
        this.retryIntervalsSeconds = new ArrayList<Integer>(retryIntervalsSeconds);
    }

    public boolean isAlertEnabled() {
        return alertEnabled;
    }

    public void setAlertEnabled(boolean alertEnabled) {
        this.alertEnabled = alertEnabled;
    }

    public String getAlertTemplateCode() {
        return alertTemplateCode;
    }

    public void setAlertTemplateCode(String alertTemplateCode) {
        this.alertTemplateCode = alertTemplateCode;
    }
}
