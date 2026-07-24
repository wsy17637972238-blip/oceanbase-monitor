package com.example.obinspection.domain.model;

import java.time.LocalDateTime;

/**
 * 告警。
 */
public class Alert {

    /** 待确认 */
    public static final String STATUS_PENDING = "PENDING";

    /** 已确认 */
    public static final String STATUS_ACKED = "ACKED";

    private Long alertId;
    private Long taskId;
    private Long resultId;
    private String level;
    private String itemName;
    private String content;
    private String status;
    private String ackedBy;
    private LocalDateTime ackedAt;
    private Integer notified;
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
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
