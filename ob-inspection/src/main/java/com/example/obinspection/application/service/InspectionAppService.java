package com.example.obinspection.application.service;

import com.example.obinspection.application.dto.request.TriggerInspectionRequest;
import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.InspectionRuleConfig;
import com.example.obinspection.domain.model.InspectionTask;
import com.example.obinspection.domain.model.enums.TaskType;
import com.example.obinspection.domain.repository.InspectionResultRepository;
import com.example.obinspection.domain.repository.InspectionRuleConfigRepository;
import com.example.obinspection.domain.repository.InspectionTaskRepository;
import com.example.obinspection.domain.service.IdGenerator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 巡检应用服务。
 */
@Service
public class InspectionAppService {

    /** 任务进行中状态（终态为 OK/WARN/CRITICAL，异常为 FAILED） */
    public static final String TASK_STATUS_RUNNING = "RUNNING";

    private static final String DEFAULT_COLLECTOR_TYPE = "jdbc";

    private final InspectionRuleConfigRepository ruleConfigRepository;
    private final InspectionTaskRepository taskRepository;
    private final InspectionResultRepository resultRepository;
    private final InspectionTaskExecutor taskExecutor;

    public InspectionAppService(InspectionRuleConfigRepository ruleConfigRepository,
                                InspectionTaskRepository taskRepository,
                                InspectionResultRepository resultRepository,
                                InspectionTaskExecutor taskExecutor) {
        this.ruleConfigRepository = ruleConfigRepository;
        this.taskRepository = taskRepository;
        this.resultRepository = resultRepository;
        this.taskExecutor = taskExecutor;
    }

    /**
     * 触发巡检：先落一条 RUNNING 任务并立即返回，
     * 实际「采集 → 判定 → 入库」由 {@link InspectionTaskExecutor} 异步执行。
     */
    public InspectionTask triggerInspection(TriggerInspectionRequest req) {
        InspectionTask task = new InspectionTask();
        task.setTaskId(IdGenerator.nextId());
        task.setTaskType(req.getTaskType() != null ? req.getTaskType().name() : TaskType.MANUAL.name());
        task.setCollectorType(StringUtils.hasText(req.getCollectorType())
                ? req.getCollectorType() : DEFAULT_COLLECTOR_TYPE);
        task.setOverallStatus(TASK_STATUS_RUNNING);
        task.setStartTime(LocalDateTime.now());
        task.setCreatedAt(LocalDateTime.now());
        taskRepository.save(task);

        taskExecutor.executeAsync(task.getTaskId());
        return task;
    }

    public List<InspectionTask> listTasks() {
        return taskRepository.findAll();
    }

    public List<InspectionResult> listResults(Long taskId) {
        return resultRepository.findByTaskId(taskId);
    }

    /**
     * 查询所有巡检规则配置，按 sort_order 排序。
     */
    public List<InspectionRuleConfig> listRules() {
        return ruleConfigRepository.findAll();
    }

    /**
     * 定时巡检，每 30 分钟一次。
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void scheduledInspection() {
        TriggerInspectionRequest req = new TriggerInspectionRequest();
        req.setTaskType(TaskType.SCHEDULED);
        triggerInspection(req);
    }
}
