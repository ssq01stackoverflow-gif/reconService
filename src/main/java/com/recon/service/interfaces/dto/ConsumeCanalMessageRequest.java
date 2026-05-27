package com.recon.service.interfaces.dto;

import javax.validation.constraints.NotBlank;

public class ConsumeCanalMessageRequest {

    @NotBlank
    private String rawMessage;

    public String getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }
}
