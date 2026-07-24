package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.AiDiagnosis;

import java.util.List;
import java.util.Optional;

/**
 * AI 诊断仓储接口（domain 层）。
 */
public interface AiDiagnosisRepository {

    void save(AiDiagnosis diagnosis);

    /**
     * 更新诊断记录（RUNNING → SUCCESS/FAILED 状态流转）。
     */
    void update(AiDiagnosis diagnosis);

    Optional<AiDiagnosis> findById(Long diagnosisId);

    List<AiDiagnosis> findAll();

    /**
     * 查询指定告警最新一次诊断（幂等判定 / 查询接口用）。
     */
    Optional<AiDiagnosis> findLatestByAlertId(Long alertId);
}
