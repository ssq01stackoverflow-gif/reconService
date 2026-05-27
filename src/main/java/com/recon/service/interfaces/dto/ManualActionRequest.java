package com.recon.service.interfaces.dto;

import javax.validation.constraints.NotBlank;

public class ManualActionRequest {

    @NotBlank
    private String operator;
    private String reason;
    private String remark;

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
