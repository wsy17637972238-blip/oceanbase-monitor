package com.example.obinspection.infrastructure.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosisResultParserTest {

    @Test
    void parsesWellFormedOutput() {
        String content = """
                根因分析：慢SQL突增最可能是新上线SQL未走索引导致全表扫描。
                处理建议：1. 通过 GV$OB_SQL_AUDIT 定位慢SQL；2. 补充索引或更新统计信息；3. 必要时限流。
                风险等级：MEDIUM""";

        DiagnosisResultParser.Parsed parsed = DiagnosisResultParser.parse(content);

        assertEquals("慢SQL突增最可能是新上线SQL未走索引导致全表扫描。", parsed.rootCause());
        assertTrue(parsed.suggestions().contains("GV$OB_SQL_AUDIT"));
        assertEquals("MEDIUM", parsed.riskLevel());
    }

    @Test
    void riskLevelParsedCaseInsensitivelyAndNormalizedToUpperCase() {
        String content = "根因分析：合并IO瓶颈。\n处理建议：避开高峰合并。\n风险等级：high";

        DiagnosisResultParser.Parsed parsed = DiagnosisResultParser.parse(content);

        assertEquals("合并IO瓶颈。", parsed.rootCause());
        assertEquals("HIGH", parsed.riskLevel());
    }

    @Test
    void missingRootCauseMarkerThrows() {
        String content = "处理建议：重启观察。\n风险等级：LOW";
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> DiagnosisResultParser.parse(content));
        assertTrue(e.getMessage().contains("根因分析"));
    }

    @Test
    void missingRiskLevelThrows() {
        String content = "根因分析：未知。\n处理建议：观察。";
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> DiagnosisResultParser.parse(content));
        assertTrue(e.getMessage().contains("风险等级"));
    }

    @Test
    void blankContentThrows() {
        assertThrows(IllegalArgumentException.class, () -> DiagnosisResultParser.parse("  "));
        assertThrows(IllegalArgumentException.class, () -> DiagnosisResultParser.parse(null));
    }
}
