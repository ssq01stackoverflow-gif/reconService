package com.recon.service.interfaces.controller;

import com.recon.service.application.service.ManualTaskApplicationService;
import com.recon.service.application.service.ReconQueryApplicationService;
import com.recon.service.common.ApiResponse;
import com.recon.service.domain.model.AlertLog;
import com.recon.service.domain.model.ManualActionLog;
import com.recon.service.domain.model.ReconExecutionLog;
import com.recon.service.domain.model.ReconTask;
import com.recon.service.domain.model.ReconTaskStatus;
import com.recon.service.interfaces.dto.ManualActionRequest;
import com.recon.service.interfaces.dto.TaskResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/recon/tasks")
public class ReconTaskController {

    private final ReconQueryApplicationService queryApplicationService;
    private final ManualTaskApplicationService manualTaskApplicationService;

    public ReconTaskController(ReconQueryApplicationService queryApplicationService,
                               ManualTaskApplicationService manualTaskApplicationService) {
        this.queryApplicationService = queryApplicationService;
        this.manualTaskApplicationService = manualTaskApplicationService;
    }

    @GetMapping
    public ApiResponse<List<TaskResponse>> listTasks(@RequestParam ReconTaskStatus status) {
        List<TaskResponse> responses = new ArrayList<TaskResponse>();
        for (ReconTask task : queryApplicationService.findTasksByStatus(status)) {
            responses.add(TaskResponse.from(task));
        }
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskResponse> getTask(@PathVariable Long id) {
        return ApiResponse.success(TaskResponse.from(queryApplicationService.getTask(id)));
    }

    @GetMapping("/{id}/executions")
    public ApiResponse<List<ReconExecutionLog>> getExecutionLogs(@PathVariable Long id) {
        return ApiResponse.success(queryApplicationService.findExecutionLogs(id));
    }

    @GetMapping("/{id}/alerts")
    public ApiResponse<List<AlertLog>> getAlertLogs(@PathVariable Long id) {
        return ApiResponse.success(queryApplicationService.findAlertLogs(id));
    }

    @GetMapping("/{id}/actions")
    public ApiResponse<List<ManualActionLog>> getManualActionLogs(@PathVariable Long id) {
        return ApiResponse.success(queryApplicationService.findManualActionLogs(id));
    }

    @PostMapping("/{id}/mark-need-handle")
    public ApiResponse<TaskResponse> markNeedHandle(@PathVariable Long id, @Valid @RequestBody ManualActionRequest request) {
        return ApiResponse.success(TaskResponse.from(
                manualTaskApplicationService.markNeedHandle(id, request.getOperator(), request.getReason(), request.getRemark())));
    }

    @PostMapping("/{id}/mark-no-need-handle")
    public ApiResponse<TaskResponse> markNoNeedHandle(@PathVariable Long id, @Valid @RequestBody ManualActionRequest request) {
        return ApiResponse.success(TaskResponse.from(
                manualTaskApplicationService.markNoNeedHandle(id, request.getOperator(), request.getReason(), request.getRemark())));
    }

    @PostMapping("/{id}/retry")
    public ApiResponse<TaskResponse> retry(@PathVariable Long id, @Valid @RequestBody ManualActionRequest request) {
        return ApiResponse.success(TaskResponse.from(
                manualTaskApplicationService.retry(id, request.getOperator(), request.getReason(), request.getRemark())));
    }

    @PostMapping("/{id}/notes")
    public ApiResponse<ManualActionLog> addNote(@PathVariable Long id, @Valid @RequestBody ManualActionRequest request) {
        return ApiResponse.success(
                manualTaskApplicationService.addNote(id, request.getOperator(), request.getReason(), request.getRemark()));
    }
}
