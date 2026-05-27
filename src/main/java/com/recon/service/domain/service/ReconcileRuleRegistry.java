package com.recon.service.domain.service;

import java.util.List;

public interface ReconcileRuleRegistry {

    List<ReconcileRule<?, ?>> allRules();

    ReconcileRule<?, ?> getRequired(String ruleCode);
}
