package com.recon.service.infrastructure.persistence;

import com.recon.service.domain.model.ReconExecutionLog;
import com.recon.service.domain.repository.ReconExecutionLogRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryReconExecutionLogRepository implements ReconExecutionLogRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ConcurrentMap<Long, ReconExecutionLog> logsById = new ConcurrentHashMap<Long, ReconExecutionLog>();

    @Override
    public synchronized ReconExecutionLog save(ReconExecutionLog log) {
        if (log.getId() == null) {
            log.setId(idGenerator.getAndIncrement());
        }
        logsById.put(log.getId(), log);
        return log;
    }

    @Override
    public List<ReconExecutionLog> findByTaskId(Long taskId) {
        List<ReconExecutionLog> result = new ArrayList<ReconExecutionLog>();
        for (ReconExecutionLog log : logsById.values()) {
            if (taskId.equals(log.getTaskId())) {
                result.add(log);
            }
        }
        result.sort(Comparator.comparing(ReconExecutionLog::getStartedAt));
        return result;
    }
}
