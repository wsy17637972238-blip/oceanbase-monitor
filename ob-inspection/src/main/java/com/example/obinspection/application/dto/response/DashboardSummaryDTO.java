package com.example.obinspection.application.dto.response;

import com.example.obinspection.domain.model.InspectionTask;

import java.util.List;
import java.util.Map;

/**
 * 看板汇总 DTO。
 */
public class DashboardSummaryDTO {

    private long totalTasks;
    private long totalAlerts;
    private long pendingAlerts;
    private String lastInspectionTime;
    private String overallStatus;

    /** 告警级别分布（level -> 数量），用于看板图表 */
    private Map<String, Long> alertLevelDistribution;

    /** 最近任务列表（按时间倒序，最多 10 条） */
    private List<InspectionTask> recentTasks;

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public long getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(long totalAlerts) {
        this.totalAlerts = totalAlerts;
    }

    public long getPendingAlerts() {
        return pendingAlerts;
    }

    public void setPendingAlerts(long pendingAlerts) {
        this.pendingAlerts = pendingAlerts;
    }

    public String getLastInspectionTime() {
        return lastInspectionTime;
    }

    public void setLastInspectionTime(String lastInspectionTime) {
        this.lastInspectionTime = lastInspectionTime;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public Map<String, Long> getAlertLevelDistribution() {
        return alertLevelDistribution;
    }

    public void setAlertLevelDistribution(Map<String, Long> alertLevelDistribution) {
        this.alertLevelDistribution = alertLevelDistribution;
    }

    public List<InspectionTask> getRecentTasks() {
        return recentTasks;
    }

    public void setRecentTasks(List<InspectionTask> recentTasks) {
        this.recentTasks = recentTasks;
    }
}
