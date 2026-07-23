package com.example.obinspection.interfaces.controller;

import com.example.obinspection.application.dto.request.TriggerInspectionRequest;
import com.example.obinspection.application.dto.response.Result;
import com.example.obinspection.application.service.InspectionAppService;
import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.InspectionRuleConfig;
import com.example.obinspection.domain.model.InspectionTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 巡检接口。
 */
@Tag(name = "巡检管理")
@RestController
@RequestMapping("/api/inspection")
public class InspectionController {

    private final InspectionAppService inspectionAppService;

    public InspectionController(InspectionAppService inspectionAppService) {
        this.inspectionAppService = inspectionAppService;
    }

    @Operation(summary = "手动触发巡检")
    @PostMapping("/trigger")
    public Result<InspectionTask> trigger(@RequestBody @Valid TriggerInspectionRequest req) {
        return Result.success(inspectionAppService.triggerInspection(req));
    }

    @Operation(summary = "巡检任务列表")
    @GetMapping("/tasks")
    public Result<List<InspectionTask>> listTasks() {
        return Result.success(inspectionAppService.listTasks());
    }

    @Operation(summary = "任务巡检结果")
    @GetMapping("/tasks/{taskId}/results")
    public Result<List<InspectionResult>> listResults(@PathVariable Long taskId) {
        return Result.success(inspectionAppService.listResults(taskId));
    }

    @Operation(summary = "巡检规则配置列表")
    @GetMapping("/rules")
    public Result<List<InspectionRuleConfig>> listRules() {
        return Result.success(inspectionAppService.listRules());
    }
}
