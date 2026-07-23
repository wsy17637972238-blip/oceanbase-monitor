package com.example.obinspection.domain.collector;

import com.example.obinspection.domain.model.Metric;

import java.util.List;

/**
 * 指标采集器接口（domain 层，无 Spring 依赖）。
 */
public interface MetricsCollector {

    List<Metric> collect();
}
