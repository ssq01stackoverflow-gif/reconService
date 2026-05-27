package com.recon.service.domain.repository;

import com.recon.service.domain.model.ReconTask;
import com.recon.service.domain.model.ReconTaskStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReconTaskRepository {

    ReconTask save(ReconTask task);

    Optional<ReconTask> insertIfAbsent(ReconTask task);

    Optional<ReconTask> findById(Long id);

    Optional<ReconTask> findByEventKeyAndRuleCode(String eventKey, String ruleCode);

    List<ReconTask> findReadyToRun(Instant now, int shardTotal, int shardIndex, int limit);

    List<ReconTask> findByStatus(ReconTaskStatus status);

    List<ReconTask> findRunningBefore(Instant timeoutBefore);

    boolean claim(Long taskId, long expectedVersion, String worker);
}
