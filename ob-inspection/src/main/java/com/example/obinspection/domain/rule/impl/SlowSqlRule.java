package com.example.obinspection.domain.rule.impl;

import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.MetricsSnapshot;
import com.example.obinspection.domain.rule.InspectionRule;

/**
 * 慢 SQL 监控规则（纯 POJO，阈值由构造器注入）。
 */
public class SlowSqlRule implements InspectionRule {

    public static final String RULE_ID = "slow_sql";
    public static final String RULE_NAME = "慢SQL监控";

    private final String warnThreshold;
    private final String criticalThreshold;

    public SlowSqlRule(String warnThreshold, String criticalThreshold) {
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
    public InspectionResult execute(MetricsSnapshot snapshot) {
        // TODO: 从快照中提取慢 SQL 指标，按阈值判定 OK/WARN/CRITICAL
        return null;
    }
}
