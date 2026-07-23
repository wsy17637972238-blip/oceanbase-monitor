package com.example.obinspection.application.service;

import com.example.obinspection.domain.model.AiDiagnosis;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * AI 诊断应用服务。
 */
@Service
public class AiDiagnosisAppService {

    public AiDiagnosis diagnose(Long alertId) {
        // TODO: 构造 Prompt -> 调用 DeepSeekClient -> 解析并保存诊断结果
        return null;
    }

    @Async("inspectionExecutor")
    public void diagnoseAsync(Long alertId) {
        // TODO: 异步执行 diagnose
    }
}
