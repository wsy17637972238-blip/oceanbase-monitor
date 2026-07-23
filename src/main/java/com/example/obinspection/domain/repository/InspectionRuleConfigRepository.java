package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.InspectionRuleConfig;

import java.util.List;
import java.util.Optional;

/**
 * 巡检规则配置仓储接口。
 */
public interface InspectionRuleConfigRepository {

    List<InspectionRuleConfig> findAll();

    Optional<InspectionRuleConfig> findById(String ruleId);

    /**
     * 查询所有启用（enabled = 1）的规则，按 sort_order 排序。
     */
    List<InspectionRuleConfig> findEnabled();
}
