package com.recon.service.job;

import com.recon.service.application.service.AlertApplicationService;
import org.springframework.stereotype.Component;

@Component
public class AlertScheduleJob {

    private final AlertApplicationService alertApplicationService;

    public AlertScheduleJob(AlertApplicationService alertApplicationService) {
        this.alertApplicationService = alertApplicationService;
    }

    public int run() {
        return alertApplicationService.scanAndAlert();
    }
}
