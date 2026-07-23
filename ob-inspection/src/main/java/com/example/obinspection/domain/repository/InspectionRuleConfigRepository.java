package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.InspectionRuleConfig;

import java.util.List;
import java.util.Optional;

/**
 * 巡检规则配置仓储接口（domain 层）。
 */
public interface InspectionRuleConfigRepository {

    void save(InspectionRuleConfig config);

    Optional<InspectionRuleConfig> findById(String ruleId);

    List<InspectionRuleConfig> findAll();

    /**
     * 查询所有启用（enabled = 1）的规则，按 sort_order 排序。
     */
    List<InspectionRuleConfig> findEnabled();
}
