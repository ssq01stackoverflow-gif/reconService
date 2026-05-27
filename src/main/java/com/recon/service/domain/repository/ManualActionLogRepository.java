package com.recon.service.domain.repository;

import com.recon.service.domain.model.ManualActionLog;

import java.util.List;

public interface ManualActionLogRepository {

    ManualActionLog save(ManualActionLog log);

    List<ManualActionLog> findByTaskId(Long taskId);
}
