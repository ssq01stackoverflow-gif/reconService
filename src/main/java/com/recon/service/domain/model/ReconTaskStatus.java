package com.recon.service.domain.model;

public enum ReconTaskStatus {
    INIT,
    RUNNING,
    RETRY_WAIT,
    SUCCESS,
    FAILED_FINAL,
    ALERTING,
    PAUSED,
    RESOLVED
}
