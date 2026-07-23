package com.example.obinspection.domain.model;

import java.time.LocalDateTime;

/**
 * 巡检规则配置实体，对应 inspection_rule_config。
 * 完全遵循 3NF，无冗余。
 */
public class InspectionRuleConfig {

    /** 规则标识，主键 */
    private String ruleId;

    /** 展示名称 */
    private String ruleName;

    /** 实现类全路径 */
    private String ruleClass;

    /** 分类：PERFORMANCE/CAPACITY/AVAILABILITY */
    private String category;

    /** WARN 阈值 */
    private String warnThreshold;

    /** CRITICAL 阈值 */
    private String criticalThreshold;

    /** 0禁用/1启用 */
    private Integer enabled;

    /** 排序 */
    private Integer sortOrder;

    /** 规则说明 */
    private String description;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleClass() {
        return ruleClass;
    }

    public void setRuleClass(String ruleClass) {
        this.ruleClass = ruleClass;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getWarnThreshold() {
        return warnThreshold;
    }

    public void setWarnThreshold(String warnThreshold) {
        this.warnThreshold = warnThreshold;
    }

    public String getCriticalThreshold() {
        return criticalThreshold;
    }

    public void setCriticalThreshold(String criticalThreshold) {
        this.criticalThreshold = criticalThreshold;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
