package com.example.obinspection.application.dto.response;

import java.util.List;

/**
 * 巡检报告 DTO（报告 JSON 与 HTML 渲染共用同一份数据）。
 */
public class InspectionReportDTO {

    private Long taskId;
    private String taskType;
    private String collectorType;
    private String startTime;
    private String endTime;
    private Integer durationMs;
    private String overallStatus;

    /** 健康评分（0-100） */
    private int healthScore;

    /** 计分说明（扣分规则与明细） */
    private String scoreExplanation;

    /** 总体结论 */
    private String conclusion;

    /** 巡检项明细 */
    private List<ReportItem> items;

    /** 报告生成时间 */
    private String generatedAt;

    /**
     * 报告明细项。
     */
    public static class ReportItem {

        private String itemName;
        private String itemLabel;
        private String status;
        private String metricValue;
        private String threshold;
        private String detail;

        /** 整改建议（仅异常项填充） */
        private String suggestion;

        /** AI 辅助分析建议（该任务关联告警已有 SUCCESS 诊断时附上） */
        private String aiSuggestion;

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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
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

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getSuggestion() {
            return suggestion;
        }

        public void setSuggestion(String suggestion) {
            this.suggestion = suggestion;
        }

        public String getAiSuggestion() {
            return aiSuggestion;
        }

        public void setAiSuggestion(String aiSuggestion) {
            this.aiSuggestion = aiSuggestion;
        }
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getCollectorType() {
        return collectorType;
    }

    public void setCollectorType(String collectorType) {
        this.collectorType = collectorType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public String getScoreExplanation() {
        return scoreExplanation;
    }

    public void setScoreExplanation(String scoreExplanation) {
        this.scoreExplanation = scoreExplanation;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public List<ReportItem> getItems() {
        return items;
    }

    public void setItems(List<ReportItem> items) {
        this.items = items;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}
