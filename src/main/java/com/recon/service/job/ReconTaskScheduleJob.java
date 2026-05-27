package com.recon.service.job;

import com.recon.service.application.service.ReconTaskScheduleService;
import org.springframework.stereotype.Component;

@Component
public class ReconTaskScheduleJob {

    private final ReconTaskScheduleService scheduleService;

    public ReconTaskScheduleJob(ReconTaskScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    public int run(int shardTotal, int shardIndex, String worker) {
        return scheduleService.scanAndRun(shardTotal, shardIndex, worker);
    }
}
