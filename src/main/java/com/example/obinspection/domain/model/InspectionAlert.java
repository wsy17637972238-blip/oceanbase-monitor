package com.example.obinspection.domain.model;

import com.example.obinspection.domain.model.enums.AlertLevel;

import java.time.LocalDateTime;

/**
 * 告警记录实体，对应 inspection_alert。
 *
 * 反范式说明：content 可由 itemName + metricValue + threshold 拼接生成，
 * 冗余固化是为防止后续规则阈值变更导致历史告警记录变化。
 */
public class InspectionAlert {

    /** 主键，Java IdGenerator 生成 */
    private Long alertId;

    /** 逻辑外键 -> inspection_task.task_id */
    private Long taskId;

    /** 逻辑外键 -> inspection_result.result_id，可为空 */
    private Long resultId;

    /** 告警级别：INFO/WARN/CRITICAL */
    private AlertLevel level;

    /** 告警项标识 */
    private String itemName;

    /** 告警内容（反范式固化：防止规则阈值变更导致历史告警变化） */
    private String content;

    /** 状态：PENDING/ACKED/RESOLVED */
    private String status;

    /** 确认人 */
    private String ackedBy;

    /** 确认时间 */
    private LocalDateTime ackedAt;

    /** 是否已通知：0否/1是 */
    private Integer notified;

    /** 创建时间 */
    private LocalDateTime createdAt;

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public AlertLevel getLevel() {
        return level;
    }

    public void setLevel(AlertLevel level) {
        this.level = level;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAckedBy() {
        return ackedBy;
    }

    public void setAckedBy(String ackedBy) {
        this.ackedBy = ackedBy;
    }

    public LocalDateTime getAckedAt() {
        return ackedAt;
    }

    public void setAckedAt(LocalDateTime ackedAt) {
        this.ackedAt = ackedAt;
    }

    public Integer getNotified() {
        return notified;
    }

    public void setNotified(Integer notified) {
        this.notified = notified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
