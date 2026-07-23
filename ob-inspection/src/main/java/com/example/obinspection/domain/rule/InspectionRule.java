package com.example.obinspection.domain.rule;

import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.MetricsSnapshot;

/**
 * 巡检规则接口（domain 层，无 Spring 依赖）。
 */
public interface InspectionRule {

    String getRuleName();

    String getRuleId();

    InspectionResult execute(MetricsSnapshot snapshot);
}
