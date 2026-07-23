package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.InspectionResult;

import java.util.List;
import java.util.Optional;

/**
 * 巡检结果仓储接口（domain 层）。
 */
public interface InspectionResultRepository {

    void save(InspectionResult result);

    Optional<InspectionResult> findById(Long resultId);

    List<InspectionResult> findAll();

    List<InspectionResult> findByTaskId(Long taskId);
}
