package com.recon.service.application.service;

import com.recon.service.config.ReconProperties;
import com.recon.service.domain.model.ReconTask;
import com.recon.service.domain.repository.ReconTaskRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ReconTaskScheduleService {

    private final ReconTaskRepository reconTaskRepository;
    private final ReconTaskExecutionService executionService;
    private final ReconProperties reconProperties;

    public ReconTaskScheduleService(ReconTaskRepository reconTaskRepository,
                                    ReconTaskExecutionService executionService,
                                    ReconProperties reconProperties) {
        this.reconTaskRepository = reconTaskRepository;
        this.executionService = executionService;
        this.reconProperties = reconProperties;
    }

    public int scanAndRun(int shardTotal, int shardIndex, String worker) {
        List<ReconTask> tasks = reconTaskRepository.findReadyToRun(
                Instant.now(),
                shardTotal,
                shardIndex,
                reconProperties.getTaskScanLimit());
        int executed = 0;
        for (ReconTask task : tasks) {
            boolean claimed = reconTaskRepository.claim(task.getId(), task.getVersion(), worker);
            if (!claimed) {
                continue;
            }
            if (executionService.executeClaimedTask(task.getId())) {
                executed++;
            }
        }
        return executed;
    }
}
