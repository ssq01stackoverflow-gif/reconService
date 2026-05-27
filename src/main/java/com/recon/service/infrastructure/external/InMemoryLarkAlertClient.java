package com.recon.service.infrastructure.external;

import com.recon.service.application.service.AlertClient;
import com.recon.service.application.service.AlertSendResult;
import com.recon.service.domain.model.ReconTask;
import org.springframework.stereotype.Component;

@Component
public class InMemoryLarkAlertClient implements AlertClient {

    @Override
    public AlertSendResult send(ReconTask task, String message) {
        return AlertSendResult.success("IN_MEMORY_LARK_ALERT_SENT taskId=" + task.getId());
    }
}
