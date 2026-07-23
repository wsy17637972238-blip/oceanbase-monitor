package com.example.obinspection.application.service;

import com.example.obinspection.application.dto.request.TriggerInspectionRequest;
import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.InspectionRuleConfig;
import com.example.obinspection.domain.model.InspectionTask;
import com.example.obinspection.domain.repository.InspectionRuleConfigRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 巡检应用服务。
 */
@Service
public class InspectionAppService {

    private final InspectionRuleConfigRepository ruleConfigRepository;

    public InspectionAppService(InspectionRuleConfigRepository ruleConfigRepository) {
        this.ruleConfigRepository = ruleConfigRepository;
    }

    public InspectionTask triggerInspection(TriggerInspectionRequest req) {
        // TODO: 采集指标 -> 执行规则 -> 保存任务/结果 -> 生成告警
        return null;
    }

    public List<InspectionTask> listTasks() {
        // TODO: 查询巡检任务列表
        return Collections.emptyList();
    }

    public List<InspectionResult> listResults(Long taskId) {
        // TODO: 查询指定任务的巡检结果
        return Collections.emptyList();
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
        // TODO: 构造默认请求并执行巡检
    }
}
