package com.example.obinspection.domain.service;

import com.example.obinspection.domain.rule.impl.ActiveSessionRule;
import com.example.obinspection.domain.rule.impl.MergeStatusRule;
import com.example.obinspection.domain.rule.impl.SlowSqlRule;

import java.util.Map;

/**
 * 整改建议文案库（纯 POJO）：按规则给出专业整改建议模板，
 * 用于巡检报告中异常项的处置指引。
 */
public final class RectificationAdvisor {

    private static final String DEFAULT_ADVICE =
            "请结合判定依据检查对应指标的实际值与阈值，必要时联系 DBA 进一步分析处理。";

    private static final Map<String, String> ADVICE_BY_RULE = Map.of(
            SlowSqlRule.RULE_ID,
            "1. 通过 GV$OB_SQL_AUDIT 定位具体慢 SQL（SQL_ID、耗时、执行次数）；"
                    + "2. 使用 EXPLAIN 检查执行计划，确认索引有效性，排查全表扫描；"
                    + "3. 更新相关表统计信息，避免优化器选择次优计划；"
                    + "4. 评估 SQL 改写、索引优化或热点数据打散。",
            ActiveSessionRule.RULE_ID,
            "1. 通过 GV$OB_PROCESSLIST 排查活跃会话来源，确认是否存在连接泄漏；"
                    + "2. 检查应用连接池配置（最大连接数、空闲回收）；"
                    + "3. 排查长事务与锁等待；"
                    + "4. 评估限流策略与连接数上限。",
            MergeStatusRule.RULE_ID,
            "1. 查询 CDB_OB_MAJOR_COMPACTION 确认合并出错/暂停的具体租户与原因；"
                    + "2. 检查磁盘 IO 与数据量是否导致合并超时；"
                    + "3. 确认合并窗口配置（major_freeze_duty_time）是否合理；"
                    + "4. 必要时手动触发合并（ALTER SYSTEM MAJOR FREEZE）或恢复暂停的合并。"
    );

    private RectificationAdvisor() {
    }

    /**
     * 按规则 ID 取整改建议；未知规则返回通用建议。
     */
    public static String adviceFor(String ruleId) {
        return ADVICE_BY_RULE.getOrDefault(ruleId, DEFAULT_ADVICE);
    }
}
