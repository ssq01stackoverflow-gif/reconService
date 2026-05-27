package com.recon.service.infrastructure.persistence;

import com.recon.service.domain.model.ChangeEvent;
import com.recon.service.domain.model.ChangeEventStatus;
import com.recon.service.domain.repository.ChangeEventRepository;
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
public class InMemoryChangeEventRepository implements ChangeEventRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ConcurrentMap<Long, ChangeEvent> eventsById = new ConcurrentHashMap<Long, ChangeEvent>();
    private final ConcurrentMap<String, Long> idsByEventKey = new ConcurrentHashMap<String, Long>();

    @Override
    public synchronized ChangeEvent save(ChangeEvent event) {
        Instant now = Instant.now();
        if (event.getId() == null) {
            event.setId(idGenerator.getAndIncrement());
            event.setCreatedAt(now);
        }
        event.setUpdatedAt(now);
        eventsById.put(event.getId(), event);
        idsByEventKey.put(event.getEventKey(), event.getId());
        return event;
    }

    @Override
    public synchronized Optional<ChangeEvent> insertIfAbsent(ChangeEvent event) {
        Long existingId = idsByEventKey.get(event.getEventKey());
        if (existingId != null) {
            return Optional.empty();
        }
        save(event);
        return Optional.of(event);
    }

    @Override
    public Optional<ChangeEvent> findByEventKey(String eventKey) {
        Long id = idsByEventKey.get(eventKey);
        return id == null ? Optional.<ChangeEvent>empty() : Optional.ofNullable(eventsById.get(id));
    }

    @Override
    public List<ChangeEvent> findByStatus(ChangeEventStatus status) {
        List<ChangeEvent> result = new ArrayList<ChangeEvent>();
        for (ChangeEvent event : eventsById.values()) {
            if (event.getStatus() == status) {
                result.add(event);
            }
        }
        result.sort(Comparator.comparing(ChangeEvent::getCreatedAt));
        return result;
    }

    @Override
    public List<ChangeEvent> findProcessingBefore(Instant timeoutBefore) {
        List<ChangeEvent> result = new ArrayList<ChangeEvent>();
        for (ChangeEvent event : eventsById.values()) {
            if (event.getStatus() == ChangeEventStatus.PROCESSING
                    && event.getUpdatedAt() != null
                    && event.getUpdatedAt().isBefore(timeoutBefore)) {
                result.add(event);
            }
        }
        result.sort(Comparator.comparing(ChangeEvent::getUpdatedAt));
        return result;
    }
}
