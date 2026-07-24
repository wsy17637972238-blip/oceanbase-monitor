package com.example.obinspection.interfaces.controller;

import com.example.obinspection.application.dto.request.AckAlertRequest;
import com.example.obinspection.application.dto.response.Result;
import com.example.obinspection.application.service.AiDiagnosisAppService;
import com.example.obinspection.application.service.AlertAppService;
import com.example.obinspection.domain.model.AiDiagnosis;
import com.example.obinspection.domain.model.Alert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 告警接口。
 */
@Tag(name = "告警管理")
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertAppService alertAppService;
    private final AiDiagnosisAppService aiDiagnosisAppService;

    public AlertController(AlertAppService alertAppService,
                           AiDiagnosisAppService aiDiagnosisAppService) {
        this.alertAppService = alertAppService;
        this.aiDiagnosisAppService = aiDiagnosisAppService;
    }

    @Operation(summary = "告警列表")
    @GetMapping
    public Result<List<Alert>> listAlerts(@RequestParam(required = false) String status,
                                          @RequestParam(required = false) String level) {
        return Result.success(alertAppService.listAlerts(status, level));
    }

    @Operation(summary = "确认告警")
    @PostMapping("/{id}/ack")
    public Result<Void> ack(@PathVariable Long id, @RequestBody @Valid AckAlertRequest req) {
        alertAppService.ackAlert(id, req);
        return Result.success(null);
    }

    @Operation(summary = "触发 AI 诊断")
    @PostMapping("/{id}/diagnose")
    public Result<Void> diagnose(@PathVariable Long id) {
        aiDiagnosisAppService.diagnoseAsync(id);
        return Result.success(null);
    }

    @Operation(summary = "查询 AI 诊断结果")
    @GetMapping("/{id}/diagnosis")
    public Result<AiDiagnosis> diagnosis(@PathVariable Long id) {
        return Result.success(aiDiagnosisAppService.diagnose(id));
    }
}
