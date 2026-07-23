package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.AiDiagnosis;

import java.util.List;

/**
 * AI 诊断结果仓储接口。
 */
public interface AiDiagnosisRepository {

    void save(AiDiagnosis diagnosis);

    List<AiDiagnosis> findByAlertId(Long alertId);

    List<AiDiagnosis> findByTaskId(Long taskId);
}
