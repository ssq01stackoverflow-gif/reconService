package com.recon.service.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ReconcilePolicyTests {

    @Test
    void shouldUseLastRetryIntervalWhenAttemptsExceedConfig() {
        ReconcilePolicy policy = new ReconcilePolicy(5, Arrays.asList(30, 60, 90), true, "DEFAULT");

        assertThat(policy.retryDelaySecondsForAttempt(1), equalTo(30));
        assertThat(policy.retryDelaySecondsForAttempt(2), equalTo(60));
        assertThat(policy.retryDelaySecondsForAttempt(3), equalTo(90));
        assertThat(policy.retryDelaySecondsForAttempt(4), equalTo(90));
    }
}
