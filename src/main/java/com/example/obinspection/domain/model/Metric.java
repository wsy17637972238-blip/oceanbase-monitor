package com.example.obinspection.domain.model;

import java.time.LocalDateTime;

/**
 * 采集指标值对象：一次采集得到的单个指标。
 */
public class Metric {

    /** 指标名，如 slow_sql_count */
    private String name;

    /** 指标值（统一字符串，兼容不同单位） */
    private String value;

    /** 单位，如 个/毫秒/%，无单位时为 null */
    private String unit;

    /** 采集时间 */
    private LocalDateTime timestamp;

    public Metric() {
    }

    public Metric(String name, String value, String unit, LocalDateTime timestamp) {
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
