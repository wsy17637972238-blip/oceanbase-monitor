package com.example.obinspection.application.dto.response;

/**
 * 看板汇总 DTO。
 */
public class DashboardSummaryDTO {

    private long totalTasks;
    private long totalAlerts;
    private long pendingAlerts;
    private String lastInspectionTime;
    private String overallStatus;

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
}
