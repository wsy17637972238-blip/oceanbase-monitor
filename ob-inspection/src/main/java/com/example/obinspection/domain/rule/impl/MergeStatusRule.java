package com.example.obinspection.domain.rule.impl;

import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.MetricsSnapshot;
import com.example.obinspection.domain.rule.InspectionRule;

/**
 * 合并状态监控规则（纯 POJO，阈值由构造器注入）。
 */
public class MergeStatusRule implements InspectionRule {

    public static final String RULE_ID = "merge_status";
    public static final String RULE_NAME = "合并状态监控";

    private final String warnThreshold;
    private final String criticalThreshold;

    public MergeStatusRule(String warnThreshold, String criticalThreshold) {
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
        // TODO: 检查 Zone 合并状态，异常时告警
        return null;
    }
}
