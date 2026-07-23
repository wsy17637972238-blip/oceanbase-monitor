package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.InspectionResult;

import java.util.List;

/**
 * 巡检结果明细仓储接口。
 */
public interface InspectionResultRepository {

    void save(InspectionResult result);

    void saveAll(List<InspectionResult> results);

    List<InspectionResult> findByTaskId(Long taskId);
}
