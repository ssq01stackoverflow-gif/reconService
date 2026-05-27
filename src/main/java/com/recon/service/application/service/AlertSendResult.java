package com.recon.service.application.service;

public class AlertSendResult {

    private final boolean success;
    private final String responsePayload;
    private final String errorMessage;

    private AlertSendResult(boolean success, String responsePayload, String errorMessage) {
        this.success = success;
        this.responsePayload = responsePayload;
        this.errorMessage = errorMessage;
    }

    public static AlertSendResult success(String responsePayload) {
        return new AlertSendResult(true, responsePayload, null);
    }

    public static AlertSendResult failure(String errorMessage) {
        return new AlertSendResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
