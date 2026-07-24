package com.example.obinspection.domain.service;

import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.InspectionResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertGeneratorTest {

    private final AlertGenerator generator = new AlertGenerator();

    private static InspectionResult result(String status) {
        InspectionResult result = new InspectionResult();
        result.setResultId(1001L);
        result.setTaskId(2002L);
        result.setItemName("slow_sql.count@obce");
        result.setItemLabel("慢SQL监控");
        result.setMetricValue("10");
        result.setStatus(status);
        result.setDetail("实例[obce]过去1小时执行超过1秒的慢SQL共10条，阈值 warn>=5 / critical>=20，判定" + status);
        result.setRuleId("slow_sql");
        return result;
    }

    @Test
    void okResultProducesNoAlert() {
        assertNull(generator.generateFrom(result("OK")));
        assertNull(generator.generateFrom(null));
    }

    @Test
    void warnResultMapsToWarnAlert() {
        Alert alert = generator.generateFrom(result("WARN"));

        assertEquals("WARN", alert.getLevel());
        assertEquals(Alert.STATUS_PENDING, alert.getStatus());
        assertEquals("slow_sql.count@obce", alert.getItemName());
        assertEquals(2002L, alert.getTaskId());
        assertEquals(1001L, alert.getResultId());
        assertEquals(0, alert.getNotified());
    }

    @Test
    void criticalResultMapsToCriticalAlert() {
        Alert alert = generator.generateFrom(result("CRITICAL"));

        assertEquals("CRITICAL", alert.getLevel());
    }

    @Test
    void contentQuotesResultDetailForTraceability() {
        Alert alert = generator.generateFrom(result("WARN"));

        assertTrue(alert.getContent().contains("[WARN]"));
        assertTrue(alert.getContent().contains("慢SQL监控"));
        assertTrue(alert.getContent().contains("慢SQL共10条"));
        assertTrue(alert.getContent().contains("warn>=5 / critical>=20"));
    }
}
