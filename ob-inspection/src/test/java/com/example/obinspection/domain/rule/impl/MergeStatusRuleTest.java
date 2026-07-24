package com.example.obinspection.domain.rule.impl;

import com.example.obinspection.domain.collector.MetricNames;
import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.Metric;
import com.example.obinspection.domain.model.MetricsSnapshot;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MergeStatusRuleTest {

    private final MergeStatusRule rule = new MergeStatusRule("IDLE", null);

    private static Metric mergeMetric(String instanceName, String tenantId,
                                      String status, String isError, String isSuspended) {
        Map<String, String> tags = new HashMap<>();
        tags.put(MetricNames.TAG_INSTANCE_ID, "1");
        tags.put(MetricNames.TAG_INSTANCE_NAME, instanceName);
        tags.put(MetricNames.TAG_TENANT_ID, tenantId);
        tags.put("is_error", isError);
        tags.put("is_suspended", isSuspended);
        return new Metric(MetricNames.MERGE_STATUS, status, "status", tags);
    }

    private static MetricsSnapshot snapshotOf(List<Metric> metrics) {
        return new MetricsSnapshot(LocalDateTime.now(), metrics);
    }

    @Test
    void okWhenAllTenantsIdle() {
        List<InspectionResult> results = rule.execute(snapshotOf(List.of(
                mergeMetric("obce", "1", "IDLE", "NO", "NO"),
                mergeMetric("obce", "1001", "IDLE", "NO", "NO"))));

        assertEquals(1, results.size());
        InspectionResult result = results.get(0);
        assertEquals("OK", result.getStatus());
        assertEquals("merge_status@obce", result.getItemName());
        assertTrue(result.getDetail().contains("tenant 1=IDLE"));
    }

    @Test
    void warnWhenTenantStatusNotIdle() {
        List<InspectionResult> results = rule.execute(snapshotOf(List.of(
                mergeMetric("obce", "1", "MERGING", "NO", "NO"))));

        assertEquals("WARN", results.get(0).getStatus());
        assertTrue(results.get(0).getDetail().contains("MERGING"));
    }

    @Test
    void warnWhenSuspended() {
        List<InspectionResult> results = rule.execute(snapshotOf(List.of(
                mergeMetric("obce", "1", "IDLE", "NO", "YES"))));

        assertEquals("WARN", results.get(0).getStatus());
        assertTrue(results.get(0).getDetail().contains("is_suspended=YES"));
    }

    @Test
    void criticalWhenMergeError() {
        List<InspectionResult> results = rule.execute(snapshotOf(List.of(
                mergeMetric("obce", "1", "IDLE", "YES", "NO"))));

        assertEquals("CRITICAL", results.get(0).getStatus());
        assertTrue(results.get(0).getDetail().contains("is_error=YES"));
    }

    @Test
    void oneResultPerInstance() {
        List<Metric> metrics = new ArrayList<>();
        metrics.add(mergeMetric("obce", "1", "IDLE", "NO", "NO"));
        metrics.add(mergeMetric("prod", "1", "IDLE", "YES", "NO"));

        List<InspectionResult> results = rule.execute(snapshotOf(metrics));

        assertEquals(2, results.size());
        assertEquals("merge_status@obce", results.get(0).getItemName());
        assertEquals("OK", results.get(0).getStatus());
        assertEquals("merge_status@prod", results.get(1).getItemName());
        assertEquals("CRITICAL", results.get(1).getStatus());
    }
}
