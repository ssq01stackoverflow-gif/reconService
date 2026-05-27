package com.recon.service.application.service;

import com.recon.service.domain.model.ReconTask;

public interface AlertClient {

    AlertSendResult send(ReconTask task, String message);
}
