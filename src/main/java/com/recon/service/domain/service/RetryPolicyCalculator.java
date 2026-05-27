package com.recon.service.domain.service;

import com.recon.service.domain.model.ReconcilePolicy;

import java.time.Instant;

public class RetryPolicyCalculator {

    public Instant nextRetryTime(Instant baseTime, int attemptCount, ReconcilePolicy policy) {
        Instant base = baseTime == null ? Instant.now() : baseTime;
        int delaySeconds = policy.retryDelaySecondsForAttempt(attemptCount);
        return base.plusSeconds(delaySeconds);
    }
}
