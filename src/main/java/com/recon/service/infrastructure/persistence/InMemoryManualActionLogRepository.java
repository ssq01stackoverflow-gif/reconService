package com.recon.service.infrastructure.persistence;

import com.recon.service.domain.model.ManualActionLog;
import com.recon.service.domain.repository.ManualActionLogRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryManualActionLogRepository implements ManualActionLogRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ConcurrentMap<Long, ManualActionLog> logsById = new ConcurrentHashMap<Long, ManualActionLog>();

    @Override
    public synchronized ManualActionLog save(ManualActionLog log) {
        if (log.getId() == null) {
            log.setId(idGenerator.getAndIncrement());
        }
        logsById.put(log.getId(), log);
        return log;
    }

    @Override
    public List<ManualActionLog> findByTaskId(Long taskId) {
        List<ManualActionLog> result = new ArrayList<ManualActionLog>();
        for (ManualActionLog log : logsById.values()) {
            if (taskId.equals(log.getTaskId())) {
                result.add(log);
            }
        }
        result.sort(Comparator.comparing(ManualActionLog::getCreatedAt));
        return result;
    }
}
