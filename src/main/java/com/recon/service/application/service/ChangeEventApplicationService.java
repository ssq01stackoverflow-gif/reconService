package com.recon.service.application.service;

import com.recon.service.domain.model.ChangeEvent;
import com.recon.service.domain.model.ChangeEventStatus;
import com.recon.service.domain.model.ReconTask;
import com.recon.service.domain.model.ReconcilePolicy;
import com.recon.service.domain.model.RuleMatchRecord;
import com.recon.service.domain.repository.ChangeEventRepository;
import com.recon.service.domain.repository.ReconTaskRepository;
import com.recon.service.domain.repository.RuleMatchRecordRepository;
import com.recon.service.domain.service.ReconcileRule;
import com.recon.service.domain.service.ReconcileRuleRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChangeEventApplicationService {

    private final ChangeEventRepository changeEventRepository;
    private final ReconTaskRepository reconTaskRepository;
    private final RuleMatchRecordRepository ruleMatchRecordRepository;
    private final ReconcileRuleRegistry ruleRegistry;

    public ChangeEventApplicationService(ChangeEventRepository changeEventRepository,
                                         ReconTaskRepository reconTaskRepository,
                                         RuleMatchRecordRepository ruleMatchRecordRepository,
                                         ReconcileRuleRegistry ruleRegistry) {
        this.changeEventRepository = changeEventRepository;
        this.reconTaskRepository = reconTaskRepository;
        this.ruleMatchRecordRepository = ruleMatchRecordRepository;
        this.ruleRegistry = ruleRegistry;
    }

    public ChangeEventProcessResult process(ChangeEvent event) {
        Optional<ChangeEvent> inserted = changeEventRepository.insertIfAbsent(event);
        if (!inserted.isPresent()) {
            return handleDuplicate(event.getEventKey());
        }

        return processExisting(inserted.get(), "PROCESSED");
    }

    private ChangeEventProcessResult processExisting(ChangeEvent savedEvent, String result) {
        savedEvent.markProcessing();
        changeEventRepository.save(savedEvent);
        List<ReconTask> createdTasks = new ArrayList<ReconTask>();
        try {
            for (ReconcileRule<?, ?> rule : ruleRegistry.allRules()) {
                if (!rule.match(savedEvent)) {
                    continue;
                }
                String bizKey = rule.bizKey(savedEvent);
                RuleMatchRecord matchRecord = new RuleMatchRecord(savedEvent.getEventKey(), rule.ruleCode(), rule.bizType(), bizKey);
                ruleMatchRecordRepository.save(matchRecord);

                ReconTask task = new ReconTask();
                task.setEventKey(savedEvent.getEventKey());
                task.setRuleCode(rule.ruleCode());
                task.setRuleName(rule.ruleCode());
                task.setBizType(rule.bizType());
                task.setBizKey(bizKey);
                ReconcilePolicy policy = rule.defaultPolicy();
                task.setMaxAttempts(policy.getMaxAttempts());

                Optional<ReconTask> created = reconTaskRepository.insertIfAbsent(task);
                created.ifPresent(createdTasks::add);
            }
            savedEvent.markProcessed();
            changeEventRepository.save(savedEvent);
            return ChangeEventProcessResult.processed(result, savedEvent, createdTasks);
        } catch (Exception ex) {
            savedEvent.markFailed(ex.getMessage());
            changeEventRepository.save(savedEvent);
            throw ex;
        }
    }

    private ChangeEventProcessResult handleDuplicate(String eventKey) {
        ChangeEvent existing = changeEventRepository.findByEventKey(eventKey)
                .orElseThrow(() -> new IllegalStateException("Duplicate event key exists but event is missing: " + eventKey));
        if (existing.getStatus() == ChangeEventStatus.PROCESSED) {
            return ChangeEventProcessResult.duplicateProcessed(existing);
        }
        if (existing.getStatus() == ChangeEventStatus.FAILED) {
            return processExisting(existing, "DUPLICATE_REPROCESSED");
        }
        return ChangeEventProcessResult.duplicateSkipped(existing);
    }
}
