package com.recon.service.interfaces.dto;

public class JobRunResponse {

    private int count;

    public JobRunResponse() {
    }

    public JobRunResponse(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
