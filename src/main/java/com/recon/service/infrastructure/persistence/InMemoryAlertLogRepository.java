package com.recon.service.infrastructure.persistence;

import com.recon.service.domain.model.AlertLog;
import com.recon.service.domain.model.AlertLogStatus;
import com.recon.service.domain.repository.AlertLogRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryAlertLogRepository implements AlertLogRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ConcurrentMap<Long, AlertLog> logsById = new ConcurrentHashMap<Long, AlertLog>();
    private final ConcurrentMap<String, Long> idsByAlertKey = new ConcurrentHashMap<String, Long>();

    @Override
    public synchronized AlertLog save(AlertLog alertLog) {
        Instant now = Instant.now();
        if (alertLog.getId() == null) {
            alertLog.setId(idGenerator.getAndIncrement());
            alertLog.setCreatedAt(now);
        }
        alertLog.setUpdatedAt(now);
        logsById.put(alertLog.getId(), alertLog);
        idsByAlertKey.put(alertLog.getAlertKey(), alertLog.getId());
        return alertLog;
    }

    @Override
    public synchronized Optional<AlertLog> insertIfAbsent(AlertLog alertLog) {
        Long existingId = idsByAlertKey.get(alertLog.getAlertKey());
        if (existingId != null) {
            return Optional.empty();
        }
        save(alertLog);
        return Optional.of(alertLog);
    }

    @Override
    public Optional<AlertLog> findByAlertKey(String alertKey) {
        Long id = idsByAlertKey.get(alertKey);
        return id == null ? Optional.<AlertLog>empty() : Optional.ofNullable(logsById.get(id));
    }

    @Override
    public List<AlertLog> findByTaskId(Long taskId) {
        List<AlertLog> result = new ArrayList<AlertLog>();
        for (AlertLog log : logsById.values()) {
            if (taskId.equals(log.getTaskId())) {
                result.add(log);
            }
        }
        result.sort(Comparator.comparing(AlertLog::getCreatedAt));
        return result;
    }

    @Override
    public List<AlertLog> findFailedReadyToRetry(Instant now, int limit) {
        List<AlertLog> result = new ArrayList<AlertLog>();
        for (AlertLog log : logsById.values()) {
            if (log.getStatus() == AlertLogStatus.FAILED
                    && (log.getNextRetryTime() == null || !log.getNextRetryTime().isAfter(now))) {
                result.add(log);
            }
        }
        result.sort(Comparator.comparing(AlertLog::getUpdatedAt));
        return limit(result, limit);
    }

    @Override
    public List<AlertLog> findProcessingBefore(Instant timeoutBefore, int limit) {
        List<AlertLog> result = new ArrayList<AlertLog>();
        for (AlertLog log : logsById.values()) {
            if (log.getStatus() == AlertLogStatus.PROCESSING
                    && log.getUpdatedAt() != null
                    && log.getUpdatedAt().isBefore(timeoutBefore)) {
                result.add(log);
            }
        }
        result.sort(Comparator.comparing(AlertLog::getUpdatedAt));
        return limit(result, limit);
    }

    private List<AlertLog> limit(List<AlertLog> logs, int limit) {
        if (logs.size() > limit) {
            return new ArrayList<AlertLog>(logs.subList(0, limit));
        }
        return logs;
    }
}
