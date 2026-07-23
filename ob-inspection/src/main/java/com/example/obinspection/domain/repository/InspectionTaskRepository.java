package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.InspectionTask;

import java.util.List;
import java.util.Optional;

/**
 * 巡检任务仓储接口（domain 层）。
 */
public interface InspectionTaskRepository {

    void save(InspectionTask task);

    Optional<InspectionTask> findById(Long taskId);

    List<InspectionTask> findAll();
}
