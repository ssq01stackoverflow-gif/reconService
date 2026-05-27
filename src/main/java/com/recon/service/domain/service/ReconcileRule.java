package com.recon.service.domain.service;

import com.recon.service.domain.model.ChangeEvent;
import com.recon.service.domain.model.CheckResult;
import com.recon.service.domain.model.PreCheckResult;
import com.recon.service.domain.model.ReconcilePolicy;

public interface ReconcileRule<L, R> {

    String ruleCode();

    String bizType();

    boolean match(ChangeEvent event);

    String bizKey(ChangeEvent event);

    default ReconcilePolicy defaultPolicy() {
        return ReconcilePolicy.defaultPolicy();
    }

    L loadLeft(ReconcileContext ctx);

    default PreCheckResult preCheck(L left, ReconcileContext ctx) {
        return PreCheckResult.pass();
    }

    R loadRight(L left, ReconcileContext ctx);

    CheckResult check(L left, R right, ReconcileContext ctx);
}
