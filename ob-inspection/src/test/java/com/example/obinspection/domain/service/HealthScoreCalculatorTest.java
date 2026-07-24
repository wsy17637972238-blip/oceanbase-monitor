package com.example.obinspection.domain.service;

import com.example.obinspection.domain.model.InspectionResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthScoreCalculatorTest {

    private static InspectionResult result(String status, String itemName) {
        InspectionResult result = new InspectionResult();
        result.setStatus(status);
        result.setItemName(itemName);
        return result;
    }

    @Test
    void allOkScoresFullMark() {
        HealthScoreCalculator.Score score = HealthScoreCalculator.calculate(List.of(
                result("OK", "a"), result("OK", "b"), result("OK", "c")));

        assertEquals(100, score.value());
        assertTrue(score.explanation().contains("无扣分项"));
    }

    @Test
    void oneWarnDeducts10() {
        HealthScoreCalculator.Score score = HealthScoreCalculator.calculate(List.of(
                result("OK", "a"), result("WARN", "slow_sql.count@obce"), result("OK", "c")));

        assertEquals(90, score.value());
        assertTrue(score.explanation().contains("WARN 1 项"));
        assertTrue(score.explanation().contains("slow_sql.count@obce"));
    }

    @Test
    void oneCriticalDeducts30() {
        HealthScoreCalculator.Score score = HealthScoreCalculator.calculate(List.of(
                result("CRITICAL", "merge_status@obce"), result("OK", "b")));

        assertEquals(70, score.value());
        assertTrue(score.explanation().contains("CRITICAL 1 项"));
    }

    @Test
    void mixedWarnAndCritical() {
        HealthScoreCalculator.Score score = HealthScoreCalculator.calculate(List.of(
                result("WARN", "a"), result("CRITICAL", "b"), result("WARN", "c"), result("OK", "d")));

        // 100 - 10 - 30 - 10 = 50
        assertEquals(50, score.value());
    }

    @Test
    void scoreFloorsAtZero() {
        HealthScoreCalculator.Score score = HealthScoreCalculator.calculate(List.of(
                result("CRITICAL", "a"), result("CRITICAL", "b"),
                result("CRITICAL", "c"), result("CRITICAL", "d")));

        // 100 - 30*4 = -20 → 下限 0
        assertEquals(0, score.value());
    }

    @Test
    void emptyResultsScoresFullMark() {
        assertEquals(100, HealthScoreCalculator.calculate(List.of()).value());
    }
}
