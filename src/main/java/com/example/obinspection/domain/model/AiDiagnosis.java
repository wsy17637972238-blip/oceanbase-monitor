package com.example.obinspection.domain.model;

import com.example.obinspection.domain.model.enums.RiskLevel;

import java.time.LocalDateTime;

/**
 * AI 智能诊断结果实体，对应 ai_diagnosis。
 *
 * 反范式说明：taskId 存在传递依赖（diagnosis_id -> alert_id -> task_id），
 * 保留是为避免与 inspection_task 的多表 JOIN，提升查询性能。
 */
public class AiDiagnosis {

    /** 主键，Java IdGenerator 生成 */
    private Long diagnosisId;

    /** 逻辑外键 -> inspection_alert.alert_id */
    private Long alertId;

    /** 逻辑外键 -> inspection_task.task_id（反范式冗余：存在传递依赖，保留为避免多表 JOIN） */
    private Long taskId;

    /** 模型名称：deepseek-chat / qwen-turbo */
    private String modelName;

    /** 发送给 LLM 的完整 Prompt */
    private String prompt;

    /** LLM 原始返回 */
    private String rawResponse;

    /** 结构化诊断结果摘要 */
    private String diagnosisResult;

    /** 根因分析 */
    private String rootCause;

    /** 优化建议 */
    private String suggestions;

    /** 风险等级：LOW/MEDIUM/HIGH */
    private RiskLevel riskLevel;

    /** 调用状态：SUCCESS/FAILED/TIMEOUT */
    private String callStatus;

    /** 失败原因 */
    private String errorMsg;

    /** 消耗 Token 数 */
    private Integer tokenUsed;

    /** 创建时间 */
    private LocalDateTime createdAt;

    public Long getDiagnosisId() {
        return diagnosisId;
    }

    public void setDiagnosisId(Long diagnosisId) {
        this.diagnosisId = diagnosisId;
    }

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

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public String getDiagnosisResult() {
        return diagnosisResult;
    }

    public void setDiagnosisResult(String diagnosisResult) {
        this.diagnosisResult = diagnosisResult;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Integer getTokenUsed() {
        return tokenUsed;
    }

    public void setTokenUsed(Integer tokenUsed) {
        this.tokenUsed = tokenUsed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
