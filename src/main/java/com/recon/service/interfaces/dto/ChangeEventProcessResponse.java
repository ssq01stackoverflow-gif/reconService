package com.recon.service.interfaces.dto;

import com.recon.service.application.service.ChangeEventProcessResult;
import com.recon.service.domain.model.ReconTask;

import java.util.ArrayList;
import java.util.List;

public class ChangeEventProcessResponse {

    private String result;
    private String eventKey;
    private List<TaskResponse> createdTasks;

    public static ChangeEventProcessResponse from(ChangeEventProcessResult result) {
        ChangeEventProcessResponse response = new ChangeEventProcessResponse();
        response.setResult(result.getResult());
        response.setEventKey(result.getEvent().getEventKey());
        List<TaskResponse> tasks = new ArrayList<TaskResponse>();
        for (ReconTask task : result.getCreatedTasks()) {
            tasks.add(TaskResponse.from(task));
        }
        response.setCreatedTasks(tasks);
        return response;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public List<TaskResponse> getCreatedTasks() {
        return createdTasks;
    }

    public void setCreatedTasks(List<TaskResponse> createdTasks) {
        this.createdTasks = createdTasks;
    }
}
