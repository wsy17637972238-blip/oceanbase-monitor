package com.example.obinspection.infrastructure.collector;

import com.example.obinspection.domain.collector.MetricsCollector;
import com.example.obinspection.domain.model.Metric;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 基于 JDBC 直连 OceanBase 的指标采集器。
 */
@Component
public class JdbcMetricsCollector implements MetricsCollector {

    private final JdbcTemplate obJdbcTemplate;

    public JdbcMetricsCollector(@Lazy JdbcTemplate obJdbcTemplate) {
        this.obJdbcTemplate = obJdbcTemplate;
    }

    @Override
    public List<Metric> collect() {
        // TODO: 通过 obJdbcTemplate 查询 OceanBase 内部视图采集指标
        return Collections.emptyList();
    }
}
