package com.example.obinspection.domain.rule.impl;

import com.example.obinspection.domain.collector.MetricNames;
import com.example.obinspection.domain.model.Metric;
import com.example.obinspection.domain.model.MetricsSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则实现共用的辅助方法（包内可见）。
 */
final class RuleSupport {

    private RuleSupport() {
    }

    /** 按指标名过滤快照中的指标 */
    static List<Metric> filterByName(MetricsSnapshot snapshot, String name) {
        List<Metric> matched = new ArrayList<>();
        for (Metric metric : snapshot.getMetrics()) {
            if (name.equals(metric.getName())) {
                matched.add(metric);
            }
        }
        return matched;
    }

    /** 取指标的来源实例名 */
    static String instanceNameOf(Metric metric) {
        return metric.getTags().getOrDefault(MetricNames.TAG_INSTANCE_NAME, "<unknown>");
    }

    /** 解析数值阈值，解析失败回退默认值 */
    static long parseThreshold(String threshold, long defaultValue) {
        try {
            return Long.parseLong(threshold.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /** 解析指标数值，解析失败按 0 处理 */
    static long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return 0L;
        }
    }
}
