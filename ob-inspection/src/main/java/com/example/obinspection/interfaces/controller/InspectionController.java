package com.example.obinspection.interfaces.controller;

import com.example.obinspection.application.dto.request.TriggerInspectionRequest;
import com.example.obinspection.application.dto.response.InspectionReportDTO;
import com.example.obinspection.application.dto.response.Result;
import com.example.obinspection.application.service.InspectionAppService;
import com.example.obinspection.application.service.InspectionReportAppService;
import com.example.obinspection.application.service.InspectionReportHtmlRenderer;
import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.InspectionRuleConfig;
import com.example.obinspection.domain.model.InspectionTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 巡检接口。
 */
@Tag(name = "巡检管理")
@RestController
@RequestMapping("/api/inspection")
public class InspectionController {

    private final InspectionAppService inspectionAppService;
    private final InspectionReportAppService reportAppService;
    private final InspectionReportHtmlRenderer reportHtmlRenderer;

    public InspectionController(InspectionAppService inspectionAppService,
                                InspectionReportAppService reportAppService,
                                InspectionReportHtmlRenderer reportHtmlRenderer) {
        this.inspectionAppService = inspectionAppService;
        this.reportAppService = reportAppService;
        this.reportHtmlRenderer = reportHtmlRenderer;
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

    @Operation(summary = "巡检报告（结构化数据）")
    @GetMapping("/tasks/{taskId}/report")
    public Result<InspectionReportDTO> report(@PathVariable Long taskId) {
        return Result.success(reportAppService.buildReport(taskId));
    }

    @Operation(summary = "下载巡检报告（HTML，可用浏览器打印导出 PDF）")
    @GetMapping("/tasks/{taskId}/report/download")
    public ResponseEntity<String> downloadReport(@PathVariable Long taskId) {
        String html = reportHtmlRenderer.render(reportAppService.buildReport(taskId));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"inspection-report-" + taskId + ".html\"")
                .body(html);
    }
}
