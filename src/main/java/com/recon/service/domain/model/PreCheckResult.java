package com.recon.service.domain.model;

public class PreCheckResult {

    private final boolean passed;
    private final String reason;

    private PreCheckResult(boolean passed, String reason) {
        this.passed = passed;
        this.reason = reason;
    }

    public static PreCheckResult pass() {
        return new PreCheckResult(true, null);
    }

    public static PreCheckResult skip(String reason) {
        return new PreCheckResult(false, reason);
    }

    public boolean isPassed() {
        return passed;
    }

    public String getReason() {
        return reason;
    }
}
