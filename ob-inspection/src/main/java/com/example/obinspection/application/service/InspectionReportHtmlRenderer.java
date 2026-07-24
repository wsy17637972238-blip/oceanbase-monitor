package com.example.obinspection.application.service;

import com.example.obinspection.application.dto.response.InspectionReportDTO;
import com.example.obinspection.domain.model.enums.InspectionStatus;
import org.springframework.stereotype.Component;

/**
 * 巡检报告 HTML 渲染器（字符串模板，无额外依赖）。
 * 输出自包含单文件 HTML，浏览器打开即可查看，可用浏览器打印功能（Ctrl+P）导出 PDF。
 */
@Component
public class InspectionReportHtmlRenderer {

    /**
     * 渲染正式巡检报告 HTML。
     */
    public String render(InspectionReportDTO report) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n")
                .append("<meta charset=\"UTF-8\">\n")
                .append("<title>巡检报告-").append(escape(String.valueOf(report.getTaskId()))).append("</title>\n")
                .append("<style>\n")
                .append("body{font-family:\"Microsoft YaHei\",SimSun,sans-serif;max-width:900px;margin:32px auto;")
                .append("padding:0 32px;color:#303133;line-height:1.7;}\n")
                .append("h1{text-align:center;font-size:24px;border-bottom:3px double #303133;padding-bottom:16px;}\n")
                .append("h2{font-size:17px;border-left:4px solid #2f54eb;padding-left:8px;margin-top:32px;}\n")
                .append("table.meta td{padding:2px 24px 2px 0;color:#606266;font-size:14px;}\n")
                .append(".score-box{text-align:center;margin:24px 0;}\n")
                .append(".score{font-size:56px;font-weight:700;}\n")
                .append(".conclusion{background:#f5f7fa;border:1px solid #e4e7ed;padding:12px 16px;")
                .append("border-radius:4px;margin:12px 0;}\n")
                .append(".explain{color:#909399;font-size:13px;}\n")
                .append("table.detail{width:100%;border-collapse:collapse;font-size:13px;}\n")
                .append("table.detail th,table.detail td{border:1px solid #dcdfe6;padding:6px 10px;")
                .append("text-align:left;vertical-align:top;}\n")
                .append("table.detail th{background:#f5f7fa;}\n")
                .append(".ok{color:#67c23a;font-weight:600;}.warn{color:#e6a23c;font-weight:600;}")
                .append(".critical{color:#f56c6c;font-weight:600;}\n")
                .append(".advice{border:1px solid #e4e7ed;border-radius:4px;padding:12px 16px;margin:12px 0;}\n")
                .append(".advice h3{margin:0 0 8px;font-size:14px;}\n")
                .append(".ai{background:#f0f5ff;border-left:3px solid #2f54eb;padding:8px 12px;margin-top:8px;")
                .append("font-size:13px;white-space:pre-wrap;}\n")
                .append(".footer{margin-top:48px;text-align:center;color:#909399;font-size:12px;")
                .append("border-top:1px solid #e4e7ed;padding-top:12px;}\n")
                .append("@media print{body{margin:0;}}\n")
                .append("</style>\n</head>\n<body>\n");

        // 标题与元信息
        html.append("<h1>OceanBase 数据库智能巡检报告</h1>\n")
                .append("<table class=\"meta\"><tr>")
                .append("<td>报告编号：").append(escape(String.valueOf(report.getTaskId()))).append("</td>")
                .append("<td>任务类型：").append(escape(taskTypeText(report.getTaskType()))).append("</td>")
                .append("<td>采集方式：").append(escape(report.getCollectorType())).append("</td>")
                .append("</tr><tr>")
                .append("<td>巡检时间：").append(escape(report.getStartTime())).append("</td>")
                .append("<td>结束时间：").append(escape(report.getEndTime())).append("</td>")
                .append("<td>执行耗时：").append(report.getDurationMs() == null ? "-"
                        : escape(String.valueOf(report.getDurationMs())) + " ms").append("</td>")
                .append("</tr></table>\n");

        // 健康评分
        html.append("<h2>一、健康评分</h2>\n")
                .append("<div class=\"score-box\"><span class=\"score\" style=\"color:")
                .append(scoreColor(report.getHealthScore())).append("\">")
                .append(report.getHealthScore()).append("</span><span style=\"font-size:20px;color:#909399\"> / 100</span></div>\n")
                .append("<p class=\"explain\">计分说明：").append(escape(report.getScoreExplanation())).append("</p>\n");

        // 总体结论
        html.append("<h2>二、总体结论</h2>\n")
                .append("<div class=\"conclusion\">总体状态：<strong>")
                .append(escape(statusText(report.getOverallStatus())))
                .append("</strong>。").append(escape(report.getConclusion())).append("</div>\n");

        // 巡检明细
        html.append("<h2>三、巡检明细</h2>\n")
                .append("<table class=\"detail\"><tr><th>巡检项</th><th>指标</th><th>状态</th>")
                .append("<th>实测值</th><th>阈值</th><th>判定依据</th></tr>\n");
        for (InspectionReportDTO.ReportItem item : report.getItems()) {
            html.append("<tr><td>").append(escape(item.getItemLabel()))
                    .append("</td><td>").append(escape(item.getItemName()))
                    .append("</td><td class=\"").append(statusClass(item.getStatus())).append("\">")
                    .append(escape(statusText(item.getStatus())))
                    .append("</td><td>").append(escape(item.getMetricValue()))
                    .append("</td><td>").append(escape(item.getThreshold()))
                    .append("</td><td>").append(escape(item.getDetail()))
                    .append("</td></tr>\n");
        }
        html.append("</table>\n");

        // 整改建议（仅异常项）
        html.append("<h2>四、异常项整改建议</h2>\n");
        boolean hasAbnormal = false;
        for (InspectionReportDTO.ReportItem item : report.getItems()) {
            if (InspectionStatus.OK.name().equals(item.getStatus())) {
                continue;
            }
            hasAbnormal = true;
            html.append("<div class=\"advice\"><h3>")
                    .append(escape(item.getItemLabel())).append("（").append(escape(item.getItemName()))
                    .append("）— ").append(escape(statusText(item.getStatus()))).append("</h3>\n")
                    .append("<div>").append(escape(item.getSuggestion())).append("</div>\n");
            if (item.getAiSuggestion() != null && !item.getAiSuggestion().isBlank()) {
                html.append("<div class=\"ai\"><strong>AI 辅助分析：</strong>\n")
                        .append(escape(item.getAiSuggestion())).append("</div>\n");
            }
            html.append("</div>\n");
        }
        if (!hasAbnormal) {
            html.append("<p>本次巡检无异常项，无需整改。</p>\n");
        }

        // 页脚
        html.append("<div class=\"footer\">本报告由 OceanBase 智能巡检系统自动生成，生成时间：")
                .append(escape(report.getGeneratedAt()))
                .append("。如需 PDF 归档，请使用浏览器打印功能（Ctrl+P）导出。</div>\n")
                .append("</body>\n</html>");
        return html.toString();
    }

    private String scoreColor(int score) {
        if (score >= 90) {
            return "#67c23a";
        }
        if (score >= 70) {
            return "#e6a23c";
        }
        return "#f56c6c";
    }

    private String statusClass(String status) {
        return switch (status) {
            case "OK" -> "ok";
            case "WARN" -> "warn";
            case "CRITICAL" -> "critical";
            default -> "";
        };
    }

    private String statusText(String status) {
        return switch (status == null ? "" : status) {
            case "OK" -> "正常";
            case "WARN" -> "警告";
            case "CRITICAL" -> "严重";
            case "FAILED" -> "执行失败";
            case "RUNNING" -> "执行中";
            default -> status == null ? "-" : status;
        };
    }

    private String taskTypeText(String taskType) {
        return switch (taskType == null ? "" : taskType) {
            case "MANUAL" -> "手动巡检";
            case "SCHEDULED" -> "定时巡检";
            default -> taskType == null ? "-" : taskType;
        };
    }

    /** HTML 转义，防止明细/AI 文本中的特殊字符破坏文档结构 */
    private String escape(String text) {
        if (text == null) {
            return "-";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
