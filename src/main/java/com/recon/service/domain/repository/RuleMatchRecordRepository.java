package com.recon.service.domain.repository;

import com.recon.service.domain.model.RuleMatchRecord;

import java.util.List;

public interface RuleMatchRecordRepository {

    RuleMatchRecord save(RuleMatchRecord record);

    List<RuleMatchRecord> findByEventKey(String eventKey);
}
