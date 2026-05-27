package com.recon.service.application.service;

import com.recon.service.domain.model.ChangeEvent;
import com.recon.service.domain.model.ChangeEventType;
import com.recon.service.domain.model.CheckResult;
import com.recon.service.domain.model.PreCheckResult;
import com.recon.service.domain.model.ReconTask;
import com.recon.service.domain.model.ReconTaskStatus;
import com.recon.service.domain.repository.ChangeEventRepository;
import com.recon.service.domain.repository.ReconTaskRepository;
import com.recon.service.domain.service.ReconcileContext;
import com.recon.service.domain.service.ReconcileRule;
import com.recon.service.domain.service.ReconcileRuleRegistry;
import com.recon.service.infrastructure.persistence.InMemoryChangeEventRepository;
import com.recon.service.infrastructure.persistence.InMemoryReconExecutionLogRepository;
import com.recon.service.infrastructure.persistence.InMemoryReconTaskRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ReconTaskExecutionServiceTests {

    @Test
    void shouldResolveWhenPreCheckSkipped() {
        Fixture fixture = new Fixture(new Rule(CheckResult.success(), false));
        fixture.claimAndExecute();

        assertThat(fixture.task().getStatus(), equalTo(ReconTaskStatus.RESOLVED));
    }

    @Test
    void shouldRetryWhenMismatchAndAttemptsRemain() {
        Fixture fixture = new Fixture(new Rule(CheckResult.mismatch("amount mismatch"), true));
        fixture.claimAndExecute();

        assertThat(fixture.task().getStatus(), equalTo(ReconTaskStatus.RETRY_WAIT));
        assertThat(fixture.task().getAttemptCount(), equalTo(1));
    }

    @Test
    void shouldFailFinalWhenMismatchReachesMaxAttempts() {
        Fixture fixture = new Fixture(new Rule(CheckResult.mismatch("amount mismatch"), true));
        fixture.task().setAttemptCount(2);
        fixture.claimAndExecute();

        assertThat(fixture.task().getStatus(), equalTo(ReconTaskStatus.FAILED_FINAL));
        assertThat(fixture.task().getAttemptCount(), equalTo(3));
    }

    private static class Fixture {

        private final ReconTaskRepository taskRepository;
        private final ReconTask task;
        private final ReconTaskExecutionService service;

        private Fixture(ReconcileRule<?, ?> rule) {
            ChangeEventRepository eventRepository = new InMemoryChangeEventRepository();
            this.taskRepository = new InMemoryReconTaskRepository();

            ChangeEvent event = new ChangeEvent();
            event.setDestination("canal-pay");
            event.setDatabaseName("pay");
            event.setTableName("pay_order");
            event.setEventType(ChangeEventType.UPDATE);
            event.setPrimaryKey("payOrderId=1");
            event.setBinlogPosition("mysql-bin.000001:100");
            event.setEventKey(ChangeEvent.buildEventKey(
                    event.getDestination(),
                    event.getDatabaseName(),
                    event.getTableName(),
                    event.getPrimaryKey(),
                    event.getEventType(),
                    event.getBinlogPosition()));
            eventRepository.save(event);

            this.task = new ReconTask();
            this.task.setEventKey(event.getEventKey());
            this.task.setRuleCode("RULE_A");
            this.task.setRuleName("RULE_A");
            this.task.setBizType("PAYMENT");
            this.task.setBizKey("payOrderId=1");
            this.task.setMaxAttempts(3);
            taskRepository.save(this.task);

            this.service = new ReconTaskExecutionService(
                    taskRepository,
                    eventRepository,
                    new InMemoryReconExecutionLogRepository(),
                    new StaticRegistry(rule));
        }

        private void claimAndExecute() {
            boolean claimed = taskRepository.claim(task.getId(), task.getVersion(), "test-worker");
            assertThat(claimed, equalTo(true));
            service.executeClaimedTask(task.getId());
        }

        private ReconTask task() {
            return taskRepository.findById(task.getId()).get();
        }
    }

    private static class Rule implements ReconcileRule<String, String> {

        private final CheckResult checkResult;
        private final boolean preCheckPass;

        private Rule(CheckResult checkResult, boolean preCheckPass) {
            this.checkResult = checkResult;
            this.preCheckPass = preCheckPass;
        }

        @Override
        public String ruleCode() {
            return "RULE_A";
        }

        @Override
        public String bizType() {
            return "PAYMENT";
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
        public PreCheckResult preCheck(String left, ReconcileContext ctx) {
            return preCheckPass ? PreCheckResult.pass() : PreCheckResult.skip("skip");
        }

        @Override
        public String loadRight(String left, ReconcileContext ctx) {
            return "right";
        }

        @Override
        public CheckResult check(String left, String right, ReconcileContext ctx) {
            return checkResult;
        }
    }

    private static class StaticRegistry implements ReconcileRuleRegistry {

        private final ReconcileRule<?, ?> rule;

        private StaticRegistry(ReconcileRule<?, ?> rule) {
            this.rule = rule;
        }

        @Override
        public List<ReconcileRule<?, ?>> allRules() {
            return Collections.singletonList(rule);
        }

        @Override
        public ReconcileRule<?, ?> getRequired(String ruleCode) {
            return rule;
        }
    }
}
