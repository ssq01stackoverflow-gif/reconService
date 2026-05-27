package com.recon.service.infrastructure.persistence;

import com.recon.service.domain.model.ReconTask;
import com.recon.service.domain.model.ReconTaskStatus;
import com.recon.service.domain.repository.ReconTaskRepository;
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
public class InMemoryReconTaskRepository implements ReconTaskRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ConcurrentMap<Long, ReconTask> tasksById = new ConcurrentHashMap<Long, ReconTask>();
    private final ConcurrentMap<String, Long> idsByEventRule = new ConcurrentHashMap<String, Long>();

    @Override
    public synchronized ReconTask save(ReconTask task) {
        Instant now = Instant.now();
        if (task.getId() == null) {
            task.setId(idGenerator.getAndIncrement());
            task.setCreatedAt(now);
        }
        task.setUpdatedAt(now);
        task.setVersion(task.getVersion() + 1);
        tasksById.put(task.getId(), task);
        idsByEventRule.put(uniqueKey(task.getEventKey(), task.getRuleCode()), task.getId());
        return task;
    }

    @Override
    public synchronized Optional<ReconTask> insertIfAbsent(ReconTask task) {
        String key = uniqueKey(task.getEventKey(), task.getRuleCode());
        Long existingId = idsByEventRule.get(key);
        if (existingId != null) {
            return Optional.empty();
        }
        save(task);
        return Optional.of(task);
    }

    @Override
    public Optional<ReconTask> findById(Long id) {
        return Optional.ofNullable(tasksById.get(id));
    }

    @Override
    public Optional<ReconTask> findByEventKeyAndRuleCode(String eventKey, String ruleCode) {
        Long id = idsByEventRule.get(uniqueKey(eventKey, ruleCode));
        return id == null ? Optional.<ReconTask>empty() : Optional.ofNullable(tasksById.get(id));
    }

    @Override
    public List<ReconTask> findReadyToRun(Instant now, int shardTotal, int shardIndex, int limit) {
        List<ReconTask> result = new ArrayList<ReconTask>();
        for (ReconTask task : tasksById.values()) {
            if (belongsToShard(task.getId(), shardTotal, shardIndex) && task.canRun(now)) {
                result.add(task);
            }
        }
        result.sort(Comparator.comparing(ReconTask::getCreatedAt));
        if (result.size() > limit) {
            return new ArrayList<ReconTask>(result.subList(0, limit));
        }
        return result;
    }

    @Override
    public List<ReconTask> findByStatus(ReconTaskStatus status) {
        List<ReconTask> result = new ArrayList<ReconTask>();
        for (ReconTask task : tasksById.values()) {
            if (task.getStatus() == status) {
                result.add(task);
            }
        }
        result.sort(Comparator.comparing(ReconTask::getCreatedAt));
        return result;
    }

    @Override
    public List<ReconTask> findRunningBefore(Instant timeoutBefore) {
        List<ReconTask> result = new ArrayList<ReconTask>();
        for (ReconTask task : tasksById.values()) {
            if (task.getStatus() == ReconTaskStatus.RUNNING
                    && task.getUpdatedAt() != null
                    && task.getUpdatedAt().isBefore(timeoutBefore)) {
                result.add(task);
            }
        }
        result.sort(Comparator.comparing(ReconTask::getUpdatedAt));
        return result;
    }

    @Override
    public synchronized boolean claim(Long taskId, long expectedVersion, String worker) {
        ReconTask task = tasksById.get(taskId);
        if (task == null || task.getVersion() != expectedVersion || !task.canRun(Instant.now())) {
            return false;
        }
        task.markRunning(worker);
        task.setVersion(task.getVersion() + 1);
        task.setUpdatedAt(Instant.now());
        return true;
    }

    private String uniqueKey(String eventKey, String ruleCode) {
        return eventKey + "|" + ruleCode;
    }

    private boolean belongsToShard(Long id, int shardTotal, int shardIndex) {
        if (shardTotal <= 1) {
            return true;
        }
        return id != null && id % shardTotal == shardIndex;
    }
}
