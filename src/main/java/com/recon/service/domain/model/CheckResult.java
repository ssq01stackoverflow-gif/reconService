package com.recon.service.domain.model;

public class CheckResult {

    private final CheckResultStatus status;
    private final String message;

    private CheckResult(CheckResultStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public static CheckResult success() {
        return new CheckResult(CheckResultStatus.SUCCESS, "success");
    }

    public static CheckResult mismatch(String message) {
        return new CheckResult(CheckResultStatus.MISMATCH, message);
    }

    public static CheckResult notReady(String message) {
        return new CheckResult(CheckResultStatus.NOT_READY, message);
    }

    public CheckResultStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
