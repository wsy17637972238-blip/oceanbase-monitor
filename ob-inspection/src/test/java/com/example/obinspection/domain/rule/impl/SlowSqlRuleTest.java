package com.example.obinspection.domain.rule.impl;

import com.example.obinspection.domain.collector.MetricNames;
import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.Metric;
import com.example.obinspection.domain.model.MetricsSnapshot;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlowSqlRuleTest {

    private final SlowSqlRule rule = new SlowSqlRule("5", "20");

    private static Metric slowSqlMetric(String value, String instanceName) {
        Map<String, String> tags = new HashMap<>();
        tags.put(MetricNames.TAG_INSTANCE_ID, "1");
        tags.put(MetricNames.TAG_INSTANCE_NAME, instanceName);
        return new Metric(MetricNames.SLOW_SQL_COUNT, value, "count", tags);
    }

    private static MetricsSnapshot snapshotOf(Metric... metrics) {
        return new MetricsSnapshot(LocalDateTime.now(), List.of(metrics));
    }

    @Test
    void okWhenBelowWarnThreshold() {
        List<InspectionResult> results = rule.execute(snapshotOf(slowSqlMetric("1", "obce")));

        assertEquals(1, results.size());
        InspectionResult result = results.get(0);
        assertEquals("OK", result.getStatus());
        assertEquals("slow_sql.count@obce", result.getItemName());
        assertEquals("1", result.getMetricValue());
        assertEquals(SlowSqlRule.RULE_ID, result.getRuleId());
        assertTrue(result.getDetail().contains("obce"));
    }

    @Test
    void warnWhenAtWarnThreshold() {
        List<InspectionResult> results = rule.execute(snapshotOf(slowSqlMetric("6", "obce")));

        assertEquals("WARN", results.get(0).getStatus());
    }

    @Test
    void criticalWhenAtCriticalThreshold() {
        List<InspectionResult> results = rule.execute(snapshotOf(slowSqlMetric("25", "obce")));

        assertEquals("CRITICAL", results.get(0).getStatus());
    }

    @Test
    void oneResultPerInstance() {
        List<InspectionResult> results = rule.execute(snapshotOf(
                slowSqlMetric("1", "obce"),
                slowSqlMetric("30", "prod")));

        assertEquals(2, results.size());
        assertEquals("slow_sql.count@obce", results.get(0).getItemName());
        assertEquals("OK", results.get(0).getStatus());
        assertEquals("slow_sql.count@prod", results.get(1).getItemName());
        assertEquals("CRITICAL", results.get(1).getStatus());
    }

    @Test
    void fallsBackToDefaultThresholdWhenUnparsable() {
        SlowSqlRule broken = new SlowSqlRule("abc", null);
        List<InspectionResult> results = broken.execute(snapshotOf(slowSqlMetric("10", "obce")));

        // 默认阈值 5/20：10 >= 5 且 < 20 → WARN
        assertEquals("WARN", results.get(0).getStatus());
        assertEquals("warn>=5, critical>=20", results.get(0).getThreshold());
    }

    @Test
    void emptyWhenMetricAbsent() {
        assertTrue(rule.execute(snapshotOf()).isEmpty());
    }
}
