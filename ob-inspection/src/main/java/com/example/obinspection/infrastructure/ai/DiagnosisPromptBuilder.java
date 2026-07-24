package com.example.obinspection.infrastructure.ai;

import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.InspectionResult;

import java.util.List;

/**
 * AI 诊断 Prompt 构造器（链式 API，纯 POJO）。
 * 组装：告警内容 + 同任务巡检结果上下文 + 固定结构输出要求（便于解析落库）。
 */
public class DiagnosisPromptBuilder {

    /** system 角色人设 */
    public static final String SYSTEM_PROMPT =
            "你是 OceanBase 数据库运维专家，擅长慢SQL、活跃会话、合并状态等巡检告警的根因分析。";

    public static final String MARKER_ROOT_CAUSE = "根因分析：";
    public static final String MARKER_SUGGESTIONS = "处理建议：";
    public static final String MARKER_RISK_LEVEL = "风险等级：";

    private Alert alert;
    private List<InspectionResult> taskResults = List.of();

    public DiagnosisPromptBuilder withAlert(Alert alert) {
        this.alert = alert;
        return this;
    }

    public DiagnosisPromptBuilder withTaskResults(List<InspectionResult> taskResults) {
        this.taskResults = taskResults == null ? List.of() : taskResults;
        return this;
    }

    public String build() {
        if (alert == null) {
            throw new IllegalStateException("alert 未设置");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("请对以下 OceanBase 巡检告警做根因分析并给出处理建议。\n\n");

        sb.append("【告警信息】\n");
        sb.append("级别：").append(alert.getLevel()).append('\n');
        sb.append("巡检项：").append(alert.getItemName()).append('\n');
        sb.append("内容：").append(alert.getContent()).append("\n\n");

        if (!taskResults.isEmpty()) {
            sb.append("【同任务巡检结果上下文】\n");
            for (InspectionResult result : taskResults) {
                sb.append("- [").append(result.getStatus()).append("] ")
                        .append(result.getItemName()).append(" = ").append(result.getMetricValue())
                        .append("（").append(result.getDetail()).append("）\n");
            }
            sb.append('\n');
        }

        sb.append("请严格按以下格式输出，不要输出任何其他内容：\n");
        sb.append(MARKER_ROOT_CAUSE).append("<一句话给出最可能的根因>\n");
        sb.append(MARKER_SUGGESTIONS).append("<分条给出可执行的处理建议>\n");
        sb.append(MARKER_RISK_LEVEL).append("<LOW/MEDIUM/HIGH 之一>");
        return sb.toString();
    }
}
