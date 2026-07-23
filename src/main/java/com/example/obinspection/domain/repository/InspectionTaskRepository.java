package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.InspectionTask;

import java.util.List;
import java.util.Optional;

/**
 * 巡检任务仓储接口。
 */
public interface InspectionTaskRepository {

    void save(InspectionTask task);

    Optional<InspectionTask> findById(Long taskId);

    /**
     * 按开始时间倒序查询最近的任务。
     *
     * @param limit 返回条数上限，分页在 Java/SQL 实现层处理
     */
    List<InspectionTask> findAllOrderByTimeDesc(int limit);
}
