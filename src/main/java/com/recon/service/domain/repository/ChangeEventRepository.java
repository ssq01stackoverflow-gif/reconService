package com.recon.service.domain.repository;

import com.recon.service.domain.model.ChangeEvent;
import com.recon.service.domain.model.ChangeEventStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChangeEventRepository {

    ChangeEvent save(ChangeEvent event);

    Optional<ChangeEvent> insertIfAbsent(ChangeEvent event);

    Optional<ChangeEvent> findByEventKey(String eventKey);

    List<ChangeEvent> findByStatus(ChangeEventStatus status);

    List<ChangeEvent> findProcessingBefore(Instant timeoutBefore);
}
