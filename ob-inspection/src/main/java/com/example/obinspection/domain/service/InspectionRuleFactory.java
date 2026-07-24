package com.example.obinspection.domain.service;

import com.example.obinspection.domain.model.InspectionRuleConfig;
import com.example.obinspection.domain.rule.InspectionRule;
import com.example.obinspection.domain.rule.impl.ActiveSessionRule;
import com.example.obinspection.domain.rule.impl.MergeStatusRule;
import com.example.obinspection.domain.rule.impl.SlowSqlRule;

/**
 * 规则工厂（纯 POJO）：按规则配置实例化规则，阈值从配置表注入（支持热调整）。
 */
public final class InspectionRuleFactory {

    private InspectionRuleFactory() {
    }

    /**
     * @throws IllegalArgumentException 未知的 rule_id
     */
    public static InspectionRule create(InspectionRuleConfig config) {
        return switch (config.getRuleId()) {
            case SlowSqlRule.RULE_ID ->
                    new SlowSqlRule(config.getWarnThreshold(), config.getCriticalThreshold());
            case ActiveSessionRule.RULE_ID ->
                    new ActiveSessionRule(config.getWarnThreshold(), config.getCriticalThreshold());
            case MergeStatusRule.RULE_ID ->
                    new MergeStatusRule(config.getWarnThreshold(), config.getCriticalThreshold());
            default -> throw new IllegalArgumentException("未知规则: " + config.getRuleId());
        };
    }
}
