package com.recon.service.domain.repository;

import com.recon.service.domain.model.AlertLog;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AlertLogRepository {

    AlertLog save(AlertLog alertLog);

    Optional<AlertLog> insertIfAbsent(AlertLog alertLog);

    Optional<AlertLog> findByAlertKey(String alertKey);

    List<AlertLog> findByTaskId(Long taskId);

    List<AlertLog> findFailedReadyToRetry(Instant now, int limit);

    List<AlertLog> findProcessingBefore(Instant timeoutBefore, int limit);
}
