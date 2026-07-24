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

class ActiveSessionRuleTest {

    private final ActiveSessionRule rule = new ActiveSessionRule("50", "100");

    private static MetricsSnapshot snapshotOfSessions(String value) {
        Map<String, String> tags = new HashMap<>();
        tags.put(MetricNames.TAG_INSTANCE_ID, "1");
        tags.put(MetricNames.TAG_INSTANCE_NAME, "obce");
        Metric metric = new Metric(MetricNames.ACTIVE_SESSION_COUNT, value, "count", tags);
        return new MetricsSnapshot(LocalDateTime.now(), List.of(metric));
    }

    @Test
    void okWhenBelowWarnThreshold() {
        List<InspectionResult> results = rule.execute(snapshotOfSessions("1"));

        assertEquals(1, results.size());
        assertEquals("OK", results.get(0).getStatus());
        assertEquals("active_session.count@obce", results.get(0).getItemName());
        assertEquals(ActiveSessionRule.RULE_ID, results.get(0).getRuleId());
    }

    @Test
    void warnWhenAtWarnThreshold() {
        assertEquals("WARN", rule.execute(snapshotOfSessions("60")).get(0).getStatus());
    }

    @Test
    void criticalWhenAtCriticalThreshold() {
        assertEquals("CRITICAL", rule.execute(snapshotOfSessions("120")).get(0).getStatus());
    }
}
