package com.example.obinspection.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 指标快照（值对象，不可变）。
 */
public class MetricsSnapshot {

    private final LocalDateTime collectedAt;
    private final List<Metric> metrics;

    public MetricsSnapshot(LocalDateTime collectedAt, List<Metric> metrics) {
        this.collectedAt = collectedAt;
        this.metrics = metrics;
    }

    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }
}
