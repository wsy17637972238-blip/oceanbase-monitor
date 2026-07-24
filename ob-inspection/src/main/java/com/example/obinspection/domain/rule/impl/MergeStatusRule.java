package com.example.obinspection.domain.rule.impl;

import com.example.obinspection.domain.collector.MetricNames;
import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.Metric;
import com.example.obinspection.domain.model.MetricsSnapshot;
import com.example.obinspection.domain.model.enums.InspectionStatus;
import com.example.obinspection.domain.rule.InspectionRule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 合并状态监控规则（纯 POJO，期望状态由构造器注入，来自 inspection_rule_config 表）。
 * 指标口径：每个租户的合并状态（CDB_OB_MAJOR_COMPACTION）。
 * 判定：任一租户 is_error=YES → CRITICAL；
 *       任一租户 is_suspended=YES 或 status != 期望状态（默认 IDLE）→ WARN；否则 OK。
 * 多租户按实例聚合成一条结果，detail 中逐租户列明状态。
 */
public class MergeStatusRule implements InspectionRule {

    public static final String RULE_ID = "merge_status";
    public static final String RULE_NAME = "合并状态监控";

    private static final String DEFAULT_EXPECTED_STATUS = "IDLE";
    private static final String YES = "YES";

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
    public List<InspectionResult> execute(MetricsSnapshot snapshot) {
        String expected = (warnThreshold == null || warnThreshold.isBlank())
                ? DEFAULT_EXPECTED_STATUS : warnThreshold.trim();

        // 合并状态指标是租户级的，按实例分组后逐实例聚合判定
        Map<String, List<Metric>> byInstance = new LinkedHashMap<>();
        for (Metric metric : RuleSupport.filterByName(snapshot, MetricNames.MERGE_STATUS)) {
            byInstance.computeIfAbsent(RuleSupport.instanceNameOf(metric), k -> new ArrayList<>())
                    .add(metric);
        }

        List<InspectionResult> results = new ArrayList<>();
        byInstance.forEach((instanceName, metrics) -> {
            List<String> errorTenants = new ArrayList<>();
            List<String> suspendedTenants = new ArrayList<>();
            List<String> abnormalTenants = new ArrayList<>();
            StringJoiner tenantStates = new StringJoiner(", ");

            for (Metric metric : metrics) {
                String tenantId = metric.getTags().getOrDefault(MetricNames.TAG_TENANT_ID, "?");
                String status = metric.getValue();
                tenantStates.add("tenant " + tenantId + "=" + status);
                if (YES.equalsIgnoreCase(metric.getTags().get("is_error"))) {
                    errorTenants.add(tenantId);
                }
                if (YES.equalsIgnoreCase(metric.getTags().get("is_suspended"))) {
                    suspendedTenants.add(tenantId);
                }
                if (!expected.equalsIgnoreCase(status)) {
                    abnormalTenants.add(tenantId + "(" + status + ")");
                }
            }

            InspectionStatus status;
            String reason;
            if (!errorTenants.isEmpty()) {
                status = InspectionStatus.CRITICAL;
                reason = "租户" + errorTenants + "合并出错(is_error=YES)";
            } else if (!suspendedTenants.isEmpty()) {
                status = InspectionStatus.WARN;
                reason = "租户" + suspendedTenants + "合并被暂停(is_suspended=YES)";
            } else if (!abnormalTenants.isEmpty()) {
                status = InspectionStatus.WARN;
                reason = "租户" + abnormalTenants + "合并状态非" + expected;
            } else {
                status = InspectionStatus.OK;
                reason = "全部租户合并状态为" + expected + "且无错误/暂停";
            }

            InspectionResult result = new InspectionResult();
            result.setRuleId(RULE_ID);
            result.setItemName(MetricNames.MERGE_STATUS + "@" + instanceName);
            result.setItemLabel(RULE_NAME);
            result.setMetricValue(tenantStates.toString());
            result.setThreshold("期望状态=" + expected + ", is_error=NO, is_suspended=NO");
            result.setStatus(status.name());
            result.setDetail(String.format("实例[%s]%s：%s，判定%s",
                    instanceName, tenantStates, reason, status.name()));
            results.add(result);
        });
        return results;
    }
}
