package com.recon.service.application.service;

import com.recon.service.domain.model.ChangeEvent;
import com.recon.service.domain.model.ChangeEventStatus;
import com.recon.service.domain.model.ChangeEventType;
import com.recon.service.domain.model.CheckResult;
import com.recon.service.domain.model.ReconTask;
import com.recon.service.domain.repository.ReconTaskRepository;
import com.recon.service.domain.service.ReconcileContext;
import com.recon.service.domain.service.ReconcileRule;
import com.recon.service.domain.service.ReconcileRuleRegistry;
import com.recon.service.infrastructure.persistence.InMemoryChangeEventRepository;
import com.recon.service.infrastructure.persistence.InMemoryReconTaskRepository;
import com.recon.service.infrastructure.persistence.InMemoryRuleMatchRecordRepository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ChangeEventApplicationServiceTests {

    @Test
    void shouldCreateOneTaskPerMatchedRuleAndSkipProcessedDuplicate() {
        ReconTaskRepository taskRepository = new InMemoryReconTaskRepository();
        ChangeEventApplicationService service = newService(taskRepository,
                new TestRule("RULE_A", "PAYMENT"),
                new TestRule("RULE_B", "PAYMENT"));

        ChangeEvent event = event("mysql-bin.000001:100", "payOrderId=1");
        ChangeEventProcessResult first = service.process(event);
        ChangeEventProcessResult duplicate = service.process(event("mysql-bin.000001:100", "payOrderId=1"));

        assertThat(first.getCreatedTasks().size(), equalTo(2));
        assertThat(duplicate.getResult(), equalTo("DUPLICATE_PROCESSED"));
        assertThat(taskRepository.findByStatus(com.recon.service.domain.model.ReconTaskStatus.INIT).size(), equalTo(2));
    }

    @Test
    void shouldAllowSameBizKeyAcrossDifferentEvents() {
        ReconTaskRepository taskRepository = new InMemoryReconTaskRepository();
        ChangeEventApplicationService service = newService(taskRepository, new TestRule("RULE_A", "PAYMENT"));

        service.process(event("mysql-bin.000001:100", "payOrderId=1"));
        service.process(event("mysql-bin.000001:101", "payOrderId=1"));

        assertThat(taskRepository.findByStatus(com.recon.service.domain.model.ReconTaskStatus.INIT).size(), equalTo(2));
        Optional<ReconTask> first = taskRepository.findByEventKeyAndRuleCode(
                ChangeEvent.buildEventKey("canal-pay", "pay", "pay_order", "payOrderId=1", ChangeEventType.UPDATE, "mysql-bin.000001:100"),
                "RULE_A");
        assertThat(first.isPresent(), equalTo(true));
    }

    private ChangeEventApplicationService newService(ReconTaskRepository taskRepository, ReconcileRule<?, ?>... rules) {
        return new ChangeEventApplicationService(
                new InMemoryChangeEventRepository(),
                taskRepository,
                new InMemoryRuleMatchRecordRepository(),
                new StaticRegistry(Arrays.asList(rules)));
    }

    private ChangeEvent event(String position, String primaryKey) {
        ChangeEvent event = new ChangeEvent();
        event.setDestination("canal-pay");
        event.setDatabaseName("pay");
        event.setTableName("pay_order");
        event.setEventType(ChangeEventType.UPDATE);
        event.setPrimaryKey(primaryKey);
        event.setBinlogPosition(position);
        event.setEventKey(ChangeEvent.buildEventKey(
                event.getDestination(),
                event.getDatabaseName(),
                event.getTableName(),
                event.getPrimaryKey(),
                event.getEventType(),
                event.getBinlogPosition()));
        event.setStatus(ChangeEventStatus.INIT);
        event.setRawMessage("{}");
        return event;
    }

    private static class TestRule implements ReconcileRule<String, String> {

        private final String ruleCode;
        private final String bizType;

        private TestRule(String ruleCode, String bizType) {
            this.ruleCode = ruleCode;
            this.bizType = bizType;
        }

        @Override
        public String ruleCode() {
            return ruleCode;
        }

        @Override
        public String bizType() {
            return bizType;
        }

        @Override
        public boolean match(ChangeEvent event) {
            return true;
        }

        @Override
        public String bizKey(ChangeEvent event) {
            return event.getPrimaryKey();
        }

        @Override
        public String loadLeft(ReconcileContext ctx) {
            return "left";
        }

        @Override
        public String loadRight(String left, ReconcileContext ctx) {
            return "right";
        }

        @Override
        public CheckResult check(String left, String right, ReconcileContext ctx) {
            return CheckResult.success();
        }
    }

    private static class StaticRegistry implements ReconcileRuleRegistry {

        private final List<ReconcileRule<?, ?>> rules;

        private StaticRegistry(List<ReconcileRule<?, ?>> rules) {
            this.rules = rules;
        }

        @Override
        public List<ReconcileRule<?, ?>> allRules() {
            return rules;
        }

        @Override
        public ReconcileRule<?, ?> getRequired(String ruleCode) {
            for (ReconcileRule<?, ?> rule : rules) {
                if (rule.ruleCode().equals(ruleCode)) {
                    return rule;
                }
            }
            throw new IllegalArgumentException(ruleCode);
        }
    }
}
