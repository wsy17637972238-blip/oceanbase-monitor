package com.example.obinspection.application.service;

import com.example.obinspection.domain.model.AiDiagnosis;
import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.repository.AiDiagnosisRepository;
import com.example.obinspection.domain.repository.AlertRepository;
import com.example.obinspection.domain.repository.InspectionResultRepository;
import com.example.obinspection.domain.service.IdGenerator;
import com.example.obinspection.infrastructure.ai.DeepSeekClient;
import com.example.obinspection.infrastructure.ai.DiagnosisPromptBuilder;
import com.example.obinspection.infrastructure.ai.DiagnosisResultParser;
import com.example.obinspection.infrastructure.ai.dto.DeepSeekRequest;
import com.example.obinspection.infrastructure.ai.dto.DeepSeekResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 诊断应用服务。
 * 异步执行：幂等判定 → 构建 Prompt → 调 DeepSeek → 解析结构化结果 → 落库。
 * 任何失败（禁用、无 key、超时、解析失败）都落 FAILED + error_msg，不影响告警主流程。
 */
@Service
public class AiDiagnosisAppService {

    private static final Logger log = LoggerFactory.getLogger(AiDiagnosisAppService.class);

    private final AlertRepository alertRepository;
    private final InspectionResultRepository resultRepository;
    private final AiDiagnosisRepository diagnosisRepository;
    private final DeepSeekClient deepSeekClient;

    @Value("${deepseek.enabled:true}")
    private boolean deepseekEnabled;

    public AiDiagnosisAppService(AlertRepository alertRepository,
                                 InspectionResultRepository resultRepository,
                                 AiDiagnosisRepository diagnosisRepository,
                                 DeepSeekClient deepSeekClient) {
        this.alertRepository = alertRepository;
        this.resultRepository = resultRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.deepSeekClient = deepSeekClient;
    }

    /**
     * 查询指定告警最新一次诊断记录（不存在返回 null）。
     */
    public AiDiagnosis diagnose(Long alertId) {
        return diagnosisRepository.findLatestByAlertId(alertId).orElse(null);
    }

    /**
     * 异步触发诊断。幂等：该告警已有 RUNNING/SUCCESS 诊断时不重复调用（避免重复消耗 token）。
     */
    @Async("inspectionExecutor")
    public void diagnoseAsync(Long alertId) {
        try {
            doDiagnose(alertId);
        } catch (Exception e) {
            // 兜底：诊断流程自身异常也不能影响调用方
            log.error("告警[{}]AI 诊断流程异常：{}", alertId, e.getMessage(), e);
        }
    }

    private void doDiagnose(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("告警不存在: " + alertId));

        var existing = diagnosisRepository.findLatestByAlertId(alertId);
        if (existing.isPresent() && !AiDiagnosis.CALL_STATUS_FAILED.equals(existing.get().getCallStatus())) {
            log.info("告警[{}]已存在{}诊断[{}]，跳过重复调用",
                    alertId, existing.get().getCallStatus(), existing.get().getDiagnosisId());
            return;
        }

        List<InspectionResult> taskResults = resultRepository.findByTaskId(alert.getTaskId());
        String prompt = new DiagnosisPromptBuilder()
                .withAlert(alert)
                .withTaskResults(taskResults)
                .build();

        AiDiagnosis diagnosis = new AiDiagnosis();
        diagnosis.setDiagnosisId(IdGenerator.nextId());
        diagnosis.setAlertId(alertId);
        diagnosis.setTaskId(alert.getTaskId());
        diagnosis.setModelName(deepSeekClient.getModel());
        diagnosis.setPrompt(prompt);
        diagnosis.setCallStatus(AiDiagnosis.CALL_STATUS_RUNNING);
        diagnosis.setCreatedAt(LocalDateTime.now());
        diagnosisRepository.save(diagnosis);

        try {
            if (!deepseekEnabled) {
                throw new IllegalStateException("AI 诊断已禁用（deepseek.enabled=false）");
            }
            DeepSeekRequest request = new DeepSeekRequest();
            request.setMessages(List.of(
                    new DeepSeekRequest.Message("system", DiagnosisPromptBuilder.SYSTEM_PROMPT),
                    new DeepSeekRequest.Message("user", prompt)));
            DeepSeekResponse response = deepSeekClient.diagnose(request);

            String content = response.getChoices().get(0).getMessage().getContent();
            DiagnosisResultParser.Parsed parsed = DiagnosisResultParser.parse(content);

            diagnosis.setRawResponse(content);
            diagnosis.setDiagnosisResult(content);
            diagnosis.setRootCause(parsed.rootCause());
            diagnosis.setSuggestions(parsed.suggestions());
            diagnosis.setRiskLevel(parsed.riskLevel());
            diagnosis.setTokenUsed(response.getUsage() != null ? response.getUsage().getTotalTokens() : null);
            diagnosis.setCallStatus(AiDiagnosis.CALL_STATUS_SUCCESS);
            diagnosisRepository.update(diagnosis);
            log.info("告警[{}]AI 诊断成功：riskLevel={}，token={}",
                    alertId, parsed.riskLevel(), diagnosis.getTokenUsed());
        } catch (Exception e) {
            diagnosis.setCallStatus(AiDiagnosis.CALL_STATUS_FAILED);
            diagnosis.setErrorMsg(e.getMessage());
            diagnosisRepository.update(diagnosis);
            log.warn("告警[{}]AI 诊断失败（已降级，不影响告警主流程）：{}", alertId, e.getMessage());
        }
    }
}
