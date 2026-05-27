package com.recon.service.infrastructure.rule;

import com.recon.service.domain.service.ReconcileRule;
import com.recon.service.domain.service.ReconcileRuleRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SpringReconcileRuleRegistry implements ReconcileRuleRegistry {

    private final List<ReconcileRule<?, ?>> rules;
    private final Map<String, ReconcileRule<?, ?>> rulesByCode;

    public SpringReconcileRuleRegistry(List<ReconcileRule<?, ?>> rules) {
        this.rules = new ArrayList<ReconcileRule<?, ?>>(rules);
        this.rulesByCode = new LinkedHashMap<String, ReconcileRule<?, ?>>();
        for (ReconcileRule<?, ?> rule : rules) {
            this.rulesByCode.put(rule.ruleCode(), rule);
        }
    }

    @Override
    public List<ReconcileRule<?, ?>> allRules() {
        return Collections.unmodifiableList(rules);
    }

    @Override
    public ReconcileRule<?, ?> getRequired(String ruleCode) {
        ReconcileRule<?, ?> rule = rulesByCode.get(ruleCode);
        if (rule == null) {
            throw new IllegalArgumentException("Reconcile rule not found: " + ruleCode);
        }
        return rule;
    }
}
