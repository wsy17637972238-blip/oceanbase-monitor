package com.example.obinspection.domain.model;

import java.util.Map;

/**
 * 指标（值对象，不可变）。
 */
public class Metric {

    private final String name;
    private final String value;
    private final String unit;
    private final Map<String, String> tags;

    public Metric(String name, String value, String unit, Map<String, String> tags) {
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public Map<String, String> getTags() {
        return tags;
    }
}
