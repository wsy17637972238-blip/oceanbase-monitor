package com.example.obinspection.domain.model;

import java.time.LocalDateTime;

/**
 * 巡检结果明细实体，对应 inspection_result。
 *
 * 反范式说明：itemLabel 可由 itemName 通过 rule_config 映射得到，冗余存储
 * 是为前端展示避免 JOIN；status 冗余存储是为方便按状态筛选查询。
 */
public class InspectionResult {

    /** 主键，Java IdGenerator 生成 */
    private Long resultId;

    /** 逻辑外键 -> inspection_task.task_id */
    private Long taskId;

    /** 巡检项：slow_sql / active_session / merge_status */
    private String itemName;

    /** 展示名称（反范式冗余：可由 itemName 经 rule_config 映射，冗余为前端展示避免 JOIN） */
    private String itemLabel;

    /** 实际指标值（统一字符串存不同单位） */
    private String metricValue;

    /** 判定阈值 */
    private String threshold;

    /** 状态：OK/WARN/CRITICAL（反范式冗余：为方便按状态筛选查询） */
    private String status;

    /** 详细说明（如慢SQL TOP3） */
    private String detail;

    /** 命中规则标识 */
    private String ruleId;

    /** 创建时间 */
    private LocalDateTime createdAt;

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
