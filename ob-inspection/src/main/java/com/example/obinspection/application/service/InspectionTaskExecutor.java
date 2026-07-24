package com.example.obinspection.application.service;

import com.example.obinspection.domain.collector.MetricsCollector;
import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.InspectionRuleConfig;
import com.example.obinspection.domain.model.InspectionTask;
import com.example.obinspection.domain.model.Metric;
import com.example.obinspection.domain.model.MetricsSnapshot;
import com.example.obinspection.domain.model.enums.InspectionStatus;
import com.example.obinspection.domain.repository.InspectionResultRepository;
import com.example.obinspection.domain.repository.InspectionRuleConfigRepository;
import com.example.obinspection.domain.repository.InspectionTaskRepository;
import com.example.obinspection.domain.rule.InspectionRule;
import com.example.obinspection.domain.service.IdGenerator;
import com.example.obinspection.domain.service.AlertGenerator;
import com.example.obinspection.domain.service.InspectionRuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 巡检任务执行器：异步执行单链路「采集 → 快照 → 规则判定 → 结果入库 → 异常告警」，
 * 并推进任务状态（RUNNING → OK/WARN/CRITICAL，异常 → FAILED）。
 */
@Service
public class InspectionTaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(InspectionTaskExecutor.class);

    private final MetricsCollector metricsCollector;
    private final InspectionTaskRepository taskRepository;
    private final InspectionResultRepository resultRepository;
    private final InspectionRuleConfigRepository ruleConfigRepository;
    private final AlertAppService alertAppService;
    private final AlertGenerator alertGenerator = new AlertGenerator();

    public InspectionTaskExecutor(MetricsCollector metricsCollector,
                                  InspectionTaskRepository taskRepository,
                                  InspectionResultRepository resultRepository,
                                  InspectionRuleConfigRepository ruleConfigRepository,
                                  AlertAppService alertAppService) {
        this.metricsCollector = metricsCollector;
        this.taskRepository = taskRepository;
        this.resultRepository = resultRepository;
        this.ruleConfigRepository = ruleConfigRepository;
        this.alertAppService = alertAppService;
    }

    @Async("inspectionExecutor")
    public void executeAsync(Long taskId) {
        long start = System.currentTimeMillis();
        try {
            List<Metric> metrics = metricsCollector.collect();
            MetricsSnapshot snapshot = new MetricsSnapshot(LocalDateTime.now(), metrics);
            log.info("任务[{}]采集完成：{} 项指标，开始规则判定", taskId, metrics.size());

            List<InspectionResult> results = new ArrayList<>();
            for (InspectionRuleConfig config : ruleConfigRepository.findEnabled()) {
                try {
                    InspectionRule rule = InspectionRuleFactory.create(config);
                    for (InspectionResult result : rule.execute(snapshot)) {
                        result.setResultId(IdGenerator.nextId());
                        result.setTaskId(taskId);
                        result.setCreatedAt(LocalDateTime.now());
                        resultRepository.save(result);
                        results.add(result);
                    }
                } catch (Exception e) {
                    // 单条规则失败不中断其他规则
                    log.warn("任务[{}]规则[{}]执行失败，跳过：{}", taskId, config.getRuleId(), e.getMessage());
                }
            }

            String overallStatus = worstStatus(results);
            generateAlerts(taskId, results);
            finishTask(taskId, overallStatus, null, start);
            log.info("任务[{}]执行完成：{} 条结果，总体状态 {}", taskId, results.size(), overallStatus);
        } catch (Exception e) {
            log.error("任务[{}]执行失败：{}", taskId, e.getMessage(), e);
            finishTask(taskId, "FAILED", e.getMessage(), start);
        }
    }

    /** 对 WARN/CRITICAL 结果生成告警；单条告警失败不影响任务与其他告警 */
    private void generateAlerts(Long taskId, List<InspectionResult> results) {
        for (InspectionResult result : results) {
            try {
                Alert alert = alertGenerator.generateFrom(result);
                if (alert != null) {
                    alertAppService.saveAlert(alert);
                }
            } catch (Exception e) {
                log.warn("任务[{}]结果[{}]生成告警失败，跳过：{}", taskId, result.getResultId(), e.getMessage());
            }
        }
    }

    /** 任务总体状态 = 所有结果中最严重级别；无结果视为 OK */
    private String worstStatus(List<InspectionResult> results) {
        String worst = InspectionStatus.OK.name();
        for (InspectionResult result : results) {
            if (InspectionStatus.CRITICAL.name().equals(result.getStatus())) {
                return InspectionStatus.CRITICAL.name();
            }
            if (InspectionStatus.WARN.name().equals(result.getStatus())) {
                worst = InspectionStatus.WARN.name();
            }
        }
        return worst;
    }

    private void finishTask(Long taskId, String overallStatus, String errorMsg, long start) {
        taskRepository.findById(taskId).ifPresent(task -> {
            task.setOverallStatus(overallStatus);
            task.setErrorMsg(errorMsg);
            task.setEndTime(LocalDateTime.now());
            task.setDurationMs((int) (System.currentTimeMillis() - start));
            taskRepository.update(task);
        });
    }
}
