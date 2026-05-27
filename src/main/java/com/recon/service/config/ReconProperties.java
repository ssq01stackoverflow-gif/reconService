package com.recon.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "recon")
public class ReconProperties {

    private int taskScanLimit = 100;
    private long processingEventTimeoutSeconds = 300;
    private long runningTaskTimeoutSeconds = 600;
    private long alertProcessingTimeoutSeconds = 300;
    private List<Integer> alertRetryIntervalsSeconds = defaultAlertRetryIntervals();
    private int alertMaxAttempts = 5;

    private static List<Integer> defaultAlertRetryIntervals() {
        List<Integer> intervals = new ArrayList<Integer>();
        intervals.add(60);
        intervals.add(300);
        intervals.add(900);
        return intervals;
    }

    public int alertRetryDelaySecondsForAttempt(int attemptCount) {
        if (alertRetryIntervalsSeconds.isEmpty()) {
            return 0;
        }
        int index = Math.max(0, attemptCount - 1);
        if (index >= alertRetryIntervalsSeconds.size()) {
            index = alertRetryIntervalsSeconds.size() - 1;
        }
        return alertRetryIntervalsSeconds.get(index);
    }

    public int getTaskScanLimit() {
        return taskScanLimit;
    }

    public void setTaskScanLimit(int taskScanLimit) {
        this.taskScanLimit = taskScanLimit;
    }

    public long getProcessingEventTimeoutSeconds() {
        return processingEventTimeoutSeconds;
    }

    public void setProcessingEventTimeoutSeconds(long processingEventTimeoutSeconds) {
        this.processingEventTimeoutSeconds = processingEventTimeoutSeconds;
    }

    public long getRunningTaskTimeoutSeconds() {
        return runningTaskTimeoutSeconds;
    }

    public void setRunningTaskTimeoutSeconds(long runningTaskTimeoutSeconds) {
        this.runningTaskTimeoutSeconds = runningTaskTimeoutSeconds;
    }

    public long getAlertProcessingTimeoutSeconds() {
        return alertProcessingTimeoutSeconds;
    }

    public void setAlertProcessingTimeoutSeconds(long alertProcessingTimeoutSeconds) {
        this.alertProcessingTimeoutSeconds = alertProcessingTimeoutSeconds;
    }

    public List<Integer> getAlertRetryIntervalsSeconds() {
        return alertRetryIntervalsSeconds;
    }

    public void setAlertRetryIntervalsSeconds(List<Integer> alertRetryIntervalsSeconds) {
        this.alertRetryIntervalsSeconds = alertRetryIntervalsSeconds == null ? defaultAlertRetryIntervals() : alertRetryIntervalsSeconds;
    }

    public int getAlertMaxAttempts() {
        return alertMaxAttempts;
    }

    public void setAlertMaxAttempts(int alertMaxAttempts) {
        this.alertMaxAttempts = alertMaxAttempts;
    }
}
