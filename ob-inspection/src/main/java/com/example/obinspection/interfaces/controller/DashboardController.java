package com.example.obinspection.interfaces.controller;

import com.example.obinspection.application.dto.response.DashboardSummaryDTO;
import com.example.obinspection.application.dto.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 看板接口。
 */
@Tag(name = "看板")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Operation(summary = "看板汇总数据")
    @GetMapping("/summary")
    public Result<DashboardSummaryDTO> summary() {
        // TODO: 聚合任务/告警统计信息
        return Result.success(null);
    }
}
