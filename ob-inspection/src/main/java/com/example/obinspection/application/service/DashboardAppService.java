package com.example.obinspection.application.service;

import com.example.obinspection.application.dto.response.DashboardSummaryDTO;
import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.InspectionTask;
import com.example.obinspection.domain.repository.AlertRepository;
import com.example.obinspection.domain.repository.InspectionTaskRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 看板应用服务：聚合任务/告警统计。
 */
@Service
public class DashboardAppService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 任务进行中状态（与 InspectionAppService 一致） */
    private static final String TASK_STATUS_RUNNING = "RUNNING";

    private final InspectionTaskRepository taskRepository;
    private final AlertRepository alertRepository;

    public DashboardAppService(InspectionTaskRepository taskRepository,
                               AlertRepository alertRepository) {
        this.taskRepository = taskRepository;
        this.alertRepository = alertRepository;
    }

    public DashboardSummaryDTO summary() {
        // findAll 均按主键倒序（最新在前）
        List<InspectionTask> tasks = taskRepository.findAll();
        List<Alert> alerts = alertRepository.findAll();

        DashboardSummaryDTO dto = new DashboardSummaryDTO();
        dto.setTotalTasks(tasks.size());
        dto.setTotalAlerts(alerts.size());
        dto.setPendingAlerts(alerts.stream()
                .filter(a -> Alert.STATUS_PENDING.equals(a.getStatus())).count());

        Map<String, Long> levelDistribution = alerts.stream()
                .collect(Collectors.groupingBy(Alert::getLevel, Collectors.counting()));
        dto.setAlertLevelDistribution(levelDistribution);

        // 当前健康状态 = 最新一个已完成任务的总体状态；无任务时为 UNKNOWN
        String overallStatus = tasks.stream()
                .map(InspectionTask::getOverallStatus)
                .filter(status -> !TASK_STATUS_RUNNING.equals(status))
                .findFirst()
                .orElse("UNKNOWN");
        dto.setOverallStatus(overallStatus);

        tasks.stream().findFirst().ifPresent(latest -> {
            if (latest.getStartTime() != null) {
                dto.setLastInspectionTime(latest.getStartTime().format(TIME_FORMAT));
            }
        });
        dto.setRecentTasks(tasks.stream().limit(10).toList());
        return dto;
    }
}
