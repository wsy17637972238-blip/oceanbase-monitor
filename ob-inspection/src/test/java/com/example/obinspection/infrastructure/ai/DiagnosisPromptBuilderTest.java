package com.example.obinspection.infrastructure.ai;

import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.InspectionResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosisPromptBuilderTest {

    private static Alert alert() {
        Alert alert = new Alert();
        alert.setLevel("WARN");
        alert.setItemName("slow_sql.count@obce");
        alert.setContent("[WARN] 慢SQL监控：实例[obce]过去1小时执行超过1秒的慢SQL共10条，阈值 warn>=5 / critical>=20，判定WARN");
        return alert;
    }

    private static InspectionResult result(String status, String itemName, String value, String detail) {
        InspectionResult result = new InspectionResult();
        result.setStatus(status);
        result.setItemName(itemName);
        result.setMetricValue(value);
        result.setDetail(detail);
        return result;
    }

    @Test
    void promptContainsAlertContextAndOutputFormat() {
        String prompt = new DiagnosisPromptBuilder()
                .withAlert(alert())
                .withTaskResults(List.of(
                        result("WARN", "slow_sql.count@obce", "10", "慢SQL共10条"),
                        result("OK", "active_session.count@obce", "1", "活跃会话共1个")))
                .build();

        assertTrue(prompt.contains("slow_sql.count@obce"));
        assertTrue(prompt.contains("慢SQL共10条"));
        assertTrue(prompt.contains("[OK] active_session.count@obce = 1"));
        assertTrue(prompt.contains(DiagnosisPromptBuilder.MARKER_ROOT_CAUSE));
        assertTrue(prompt.contains(DiagnosisPromptBuilder.MARKER_SUGGESTIONS));
        assertTrue(prompt.contains(DiagnosisPromptBuilder.MARKER_RISK_LEVEL));
        assertTrue(prompt.contains("LOW/MEDIUM/HIGH"));
    }

    @Test
    void systemPromptDeclaresOceanBaseExpert() {
        assertTrue(DiagnosisPromptBuilder.SYSTEM_PROMPT.contains("OceanBase 数据库运维专家"));
    }

    @Test
    void buildWithoutAlertThrows() {
        assertThrows(IllegalStateException.class, () -> new DiagnosisPromptBuilder().build());
    }

    @Test
    void buildWithoutTaskResultsOmitsContextSection() {
        String prompt = new DiagnosisPromptBuilder().withAlert(alert()).build();

        assertTrue(!prompt.contains("【同任务巡检结果上下文】"));
    }
}
