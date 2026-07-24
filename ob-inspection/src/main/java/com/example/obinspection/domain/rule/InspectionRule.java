package com.example.obinspection.domain.rule;

import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.MetricsSnapshot;

import java.util.List;

/**
 * 巡检规则接口（domain 层，无 Spring 依赖）。
 */
public interface InspectionRule {

    String getRuleName();

    String getRuleId();

    /**
     * 执行规则判定。多实例场景下同名指标每个实例一条，
     * 规则按实例分组判定，每个实例产出一条结果（itemName 带 @实例名 后缀区分来源）。
     * 结果的 resultId / taskId / createdAt 由调用方统一赋值。
     */
    List<InspectionResult> execute(MetricsSnapshot snapshot);
}
