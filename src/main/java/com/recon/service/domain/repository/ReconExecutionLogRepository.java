package com.recon.service.domain.repository;

import com.recon.service.domain.model.ReconExecutionLog;

import java.util.List;

public interface ReconExecutionLogRepository {

    ReconExecutionLog save(ReconExecutionLog log);

    List<ReconExecutionLog> findByTaskId(Long taskId);
}
