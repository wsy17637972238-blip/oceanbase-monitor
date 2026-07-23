package com.example.obinspection.domain.model;

import com.example.obinspection.domain.model.enums.TaskType;

import java.time.LocalDateTime;

/**
 * 巡检任务主表实体，对应 inspection_task。
 *
 * 反范式说明：overallStatus 可由 inspection_result 聚合推导，
 * 冗余存储是为避免频繁 JOIN 查询，空间换时间。
 */
public class InspectionTask {

    /** 主键，Java IdGenerator 生成 */
    private Long taskId;

    /** 触发方式：SCHEDULED定时/MANUAL手动 */
    private TaskType taskType;

    /** 整体状态：OK/WARN/CRITICAL（反范式冗余，可由结果明细聚合推导，冗余为避免频繁 JOIN，空间换时间） */
    private String overallStatus;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 执行耗时毫秒 */
    private Integer durationMs;

    /** 采集器类型：jdbc/ocp */
    private String collectorType;

    /** 异常信息 */
    private String errorMsg;

    /** 创建时间 */
    private LocalDateTime createdAt;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

    public String getCollectorType() {
        return collectorType;
    }

    public void setCollectorType(String collectorType) {
        this.collectorType = collectorType;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
