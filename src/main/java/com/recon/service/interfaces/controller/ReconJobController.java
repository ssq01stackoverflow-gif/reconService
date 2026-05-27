package com.recon.service.interfaces.controller;

import com.recon.service.common.ApiResponse;
import com.recon.service.interfaces.dto.JobRunResponse;
import com.recon.service.job.AlertScheduleJob;
import com.recon.service.job.CompensationJob;
import com.recon.service.job.ReconTaskScheduleJob;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recon/jobs")
public class ReconJobController {

    private final ReconTaskScheduleJob reconTaskScheduleJob;
    private final AlertScheduleJob alertScheduleJob;
    private final CompensationJob compensationJob;

    public ReconJobController(ReconTaskScheduleJob reconTaskScheduleJob,
                              AlertScheduleJob alertScheduleJob,
                              CompensationJob compensationJob) {
        this.reconTaskScheduleJob = reconTaskScheduleJob;
        this.alertScheduleJob = alertScheduleJob;
        this.compensationJob = compensationJob;
    }

    @PostMapping("/tasks")
    public ApiResponse<JobRunResponse> runTasks(@RequestParam(defaultValue = "1") int shardTotal,
                                                @RequestParam(defaultValue = "0") int shardIndex,
                                                @RequestParam(defaultValue = "manual") String worker) {
        return ApiResponse.success(new JobRunResponse(reconTaskScheduleJob.run(shardTotal, shardIndex, worker)));
    }

    @PostMapping("/alerts")
    public ApiResponse<JobRunResponse> runAlerts() {
        return ApiResponse.success(new JobRunResponse(alertScheduleJob.run()));
    }

    @PostMapping("/compensation/events")
    public ApiResponse<JobRunResponse> recoverEvents() {
        return ApiResponse.success(new JobRunResponse(compensationJob.recoverProcessingEvents()));
    }

    @PostMapping("/compensation/tasks")
    public ApiResponse<JobRunResponse> recoverTasks() {
        return ApiResponse.success(new JobRunResponse(compensationJob.recoverRunningTasks()));
    }

    @PostMapping("/compensation/alerts")
    public ApiResponse<JobRunResponse> compensateAlerts() {
        return ApiResponse.success(new JobRunResponse(compensationJob.compensateAlerts()));
    }
}
