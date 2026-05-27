package com.recon.service.job;

import com.recon.service.application.service.CompensationApplicationService;
import org.springframework.stereotype.Component;

@Component
public class CompensationJob {

    private final CompensationApplicationService compensationApplicationService;

    public CompensationJob(CompensationApplicationService compensationApplicationService) {
        this.compensationApplicationService = compensationApplicationService;
    }

    public int recoverProcessingEvents() {
        return compensationApplicationService.recoverProcessingEvents();
    }

    public int recoverRunningTasks() {
        return compensationApplicationService.recoverRunningTasks();
    }

    public int compensateAlerts() {
        return compensationApplicationService.compensateAlerts();
    }
}
