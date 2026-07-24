package com.example.obinspection.application.service;

import com.example.obinspection.application.dto.response.InspectionReportDTO;
import com.example.obinspection.domain.model.AiDiagnosis;
import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.InspectionTask;
import com.example.obinspection.domain.model.enums.InspectionStatus;
import com.example.obinspection.domain.repository.AiDiagnosisRepository;
import com.example.obinspection.domain.repository.AlertRepository;
import com.example.obinspection.domain.repository.InspectionResultRepository;
import com.example.obinspection.domain.repository.InspectionTaskRepository;
import com.example.obinspection.domain.service.HealthScoreCalculator;
import com.example.obinspection.domain.service.RectificationAdvisor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 巡检报告应用服务：组装报告数据（健康评分 + 总体结论 + 明细 + 整改建议）。
 */
@Service
public class InspectionReportAppService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final InspectionTaskRepository taskRepository;
    private final InspectionResultRepository resultRepository;
    private final AlertRepository alertRepository;
    private final AiDiagnosisRepository diagnosisRepository;

    public InspectionReportAppService(InspectionTaskRepository taskRepository,
                                      InspectionResultRepository resultRepository,
                                      AlertRepository alertRepository,
                                      AiDiagnosisRepository diagnosisRepository) {
        this.taskRepository = taskRepository;
        this.resultRepository = resultRepository;
        this.alertRepository = alertRepository;
        this.diagnosisRepository = diagnosisRepository;
    }

    /**
     * 生成指定任务的巡检报告数据。
     *
     * @throws IllegalArgumentException 任务不存在
     */
    public InspectionReportDTO buildReport(Long taskId) {
        InspectionTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("巡检任务不存在: " + taskId));
        List<InspectionResult> results = resultRepository.findByTaskId(taskId);

        HealthScoreCalculator.Score score = HealthScoreCalculator.calculate(results);

        // 该任务关联告警的 SUCCESS AI 诊断建议，按 item_name 归档
        Map<String, String> aiSuggestionByItem = new HashMap<>();
        for (Alert alert : alertRepository.findByTaskId(taskId)) {
            diagnosisRepository.findLatestByAlertId(alert.getAlertId())
                    .filter(d -> AiDiagnosis.CALL_STATUS_SUCCESS.equals(d.getCallStatus()))
                    .ifPresent(d -> aiSuggestionByItem.put(alert.getItemName(), d.getSuggestions()));
        }

        List<InspectionReportDTO.ReportItem> items = results.stream().map(result -> {
            InspectionReportDTO.ReportItem item = new InspectionReportDTO.ReportItem();
            item.setItemName(result.getItemName());
            item.setItemLabel(result.getItemLabel());
            item.setStatus(result.getStatus());
            item.setMetricValue(result.getMetricValue());
            item.setThreshold(result.getThreshold());
            item.setDetail(result.getDetail());
            if (!InspectionStatus.OK.name().equals(result.getStatus())) {
                item.setSuggestion(RectificationAdvisor.adviceFor(result.getRuleId()));
                item.setAiSuggestion(aiSuggestionByItem.get(result.getItemName()));
            }
            return item;
        }).toList();

        InspectionReportDTO dto = new InspectionReportDTO();
        dto.setTaskId(task.getTaskId());
        dto.setTaskType(task.getTaskType());
        dto.setCollectorType(task.getCollectorType());
        dto.setStartTime(format(task.getStartTime()));
        dto.setEndTime(format(task.getEndTime()));
        dto.setDurationMs(task.getDurationMs());
        dto.setOverallStatus(task.getOverallStatus());
        dto.setHealthScore(score.value());
        dto.setScoreExplanation(score.explanation());
        dto.setConclusion(buildConclusion(score.value(), results));
        dto.setItems(items);
        dto.setGeneratedAt(LocalDateTime.now().format(TIME_FORMAT));
        return dto;
    }

    /**
     * 总体结论（按分数分档，结论先行）。
     */
    private String buildConclusion(int score, List<InspectionResult> results) {
        long warnCount = results.stream()
                .filter(r -> InspectionStatus.WARN.name().equals(r.getStatus())).count();
        long criticalCount = results.stream()
                .filter(r -> InspectionStatus.CRITICAL.name().equals(r.getStatus())).count();

        if (criticalCount > 0) {
            return String.format("本次巡检发现 %d 项严重异常、%d 项警告，集群存在严重风险，"
                    + "请立即按整改建议处理严重异常项，并复查相关指标。", criticalCount, warnCount);
        }
        if (score >= 90) {
            return warnCount == 0
                    ? "本次巡检各项核心指标全部正常，集群健康状况良好，无需处置。"
                    : String.format("本次巡检整体健康状况良好，发现 %d 项警告，建议按计划处理并持续观察。", warnCount);
        }
        if (score >= 70) {
            return String.format("本次巡检发现 %d 项警告，集群存在一定风险，"
                    + "建议尽快按整改建议处理并复测确认。", warnCount);
        }
        return String.format("本次巡检发现 %d 项警告，健康评分偏低，集群存在明显风险，"
                + "请优先处理警告项并分析根因。", warnCount);
    }

    private String format(LocalDateTime time) {
        return time == null ? null : time.format(TIME_FORMAT);
    }
}
