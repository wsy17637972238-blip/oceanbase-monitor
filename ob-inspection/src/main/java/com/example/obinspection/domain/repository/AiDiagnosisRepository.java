package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.AiDiagnosis;

import java.util.List;
import java.util.Optional;

/**
 * AI 诊断仓储接口（domain 层）。
 */
public interface AiDiagnosisRepository {

    void save(AiDiagnosis diagnosis);

    Optional<AiDiagnosis> findById(Long diagnosisId);

    List<AiDiagnosis> findAll();
}
