package com.example.obinspection.application.dto.request;

import com.example.obinspection.domain.model.enums.TaskType;

/**
 * 手动触发巡检请求。
 */
public class TriggerInspectionRequest {

    /** 任务类型（可选，默认 MANUAL） */
    private TaskType taskType;

    /** 采集器类型（可选，默认 jdbc） */
    private String collectorType;

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getCollectorType() {
        return collectorType;
    }

    public void setCollectorType(String collectorType) {
        this.collectorType = collectorType;
    }
}
