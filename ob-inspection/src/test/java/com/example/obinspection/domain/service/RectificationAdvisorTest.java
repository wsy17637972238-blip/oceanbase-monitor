package com.example.obinspection.domain.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RectificationAdvisorTest {

    @Test
    void slowSqlAdviceMentionsExplainAndIndex() {
        String advice = RectificationAdvisor.adviceFor("slow_sql");

        assertTrue(advice.contains("EXPLAIN"));
        assertTrue(advice.contains("索引"));
    }

    @Test
    void activeSessionAdviceMentionsConnectionPool() {
        String advice = RectificationAdvisor.adviceFor("active_session");

        assertTrue(advice.contains("连接"));
    }

    @Test
    void mergeStatusAdviceMentionsMajorFreeze() {
        String advice = RectificationAdvisor.adviceFor("merge_status");

        assertTrue(advice.contains("MAJOR FREEZE"));
    }

    @Test
    void unknownRuleFallsBackToDefaultAdvice() {
        String advice = RectificationAdvisor.adviceFor("unknown_rule");

        assertTrue(advice.contains("DBA"));
    }

    @Test
    void adviceIsDeterministic() {
        assertEquals(RectificationAdvisor.adviceFor("slow_sql"),
                RectificationAdvisor.adviceFor("slow_sql"));
    }
}
