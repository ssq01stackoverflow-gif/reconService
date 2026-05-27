package com.recon.service.infrastructure.persistence;

import com.recon.service.domain.model.RuleMatchRecord;
import com.recon.service.domain.repository.RuleMatchRecordRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryRuleMatchRecordRepository implements RuleMatchRecordRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ConcurrentMap<Long, RuleMatchRecord> recordsById = new ConcurrentHashMap<Long, RuleMatchRecord>();

    @Override
    public synchronized RuleMatchRecord save(RuleMatchRecord record) {
        if (record.getId() == null) {
            record.setId(idGenerator.getAndIncrement());
        }
        recordsById.put(record.getId(), record);
        return record;
    }

    @Override
    public List<RuleMatchRecord> findByEventKey(String eventKey) {
        List<RuleMatchRecord> result = new ArrayList<RuleMatchRecord>();
        for (RuleMatchRecord record : recordsById.values()) {
            if (eventKey.equals(record.getEventKey())) {
                result.add(record);
            }
        }
        result.sort(Comparator.comparing(RuleMatchRecord::getCreatedAt));
        return result;
    }
}
