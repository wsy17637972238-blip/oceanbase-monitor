package com.example.obinspection.domain.rule.impl;

import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.MetricsSnapshot;
import com.example.obinspection.domain.rule.InspectionRule;

/**
 * 活跃会话监控规则（纯 POJO，阈值由构造器注入）。
 */
public class ActiveSessionRule implements InspectionRule {

    public static final String RULE_ID = "active_session";
    public static final String RULE_NAME = "活跃会话监控";

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
    public InspectionResult execute(MetricsSnapshot snapshot) {
        // TODO: 统计非 Sleep 状态会话数，按阈值判定 OK/WARN/CRITICAL
        return null;
    }
}
