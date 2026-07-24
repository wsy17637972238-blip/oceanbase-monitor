package com.example.obinspection.domain.rule.impl;

import com.example.obinspection.domain.collector.MetricNames;
import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.Metric;
import com.example.obinspection.domain.model.MetricsSnapshot;
import com.example.obinspection.domain.model.enums.InspectionStatus;
import com.example.obinspection.domain.rule.InspectionRule;

import java.util.ArrayList;
import java.util.List;

/**
 * 活跃会话监控规则（纯 POJO，阈值由构造器注入，来自 inspection_rule_config 表）。
 * 指标口径：非 Sleep 状态的会话数。
 */
public class ActiveSessionRule implements InspectionRule {

    public static final String RULE_ID = "active_session";
    public static final String RULE_NAME = "活跃会话监控";

    private static final long DEFAULT_WARN = 50L;
    private static final long DEFAULT_CRITICAL = 100L;

    private final String warnThreshold;
    private final String criticalThreshold;

    public ActiveSessionRule(String warnThreshold, String criticalThreshold) {
        this.warnThreshold = warnThreshold;
        this.criticalThreshold = criticalThreshold;
    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public List<InspectionResult> execute(MetricsSnapshot snapshot) {
        long warn = RuleSupport.parseThreshold(warnThreshold, DEFAULT_WARN);
        long critical = RuleSupport.parseThreshold(criticalThreshold, DEFAULT_CRITICAL);

        List<InspectionResult> results = new ArrayList<>();
        for (Metric metric : RuleSupport.filterByName(snapshot, MetricNames.ACTIVE_SESSION_COUNT)) {
            String instanceName = RuleSupport.instanceNameOf(metric);
            long value = RuleSupport.parseLong(metric.getValue());

            InspectionStatus status;
            if (value >= critical) {
                status = InspectionStatus.CRITICAL;
            } else if (value >= warn) {
                status = InspectionStatus.WARN;
            } else {
                status = InspectionStatus.OK;
            }

            InspectionResult result = new InspectionResult();
            result.setRuleId(RULE_ID);
            result.setItemName(MetricNames.ACTIVE_SESSION_COUNT + "@" + instanceName);
            result.setItemLabel(RULE_NAME);
            result.setMetricValue(String.valueOf(value));
            result.setThreshold("warn>=" + warn + ", critical>=" + critical);
            result.setStatus(status.name());
            result.setDetail(String.format(
                    "实例[%s]非Sleep状态的活跃会话共%d个，阈值 warn>=%d / critical>=%d，判定%s",
                    instanceName, value, warn, critical, status.name()));
            results.add(result);
        }
        return results;
    }
}
