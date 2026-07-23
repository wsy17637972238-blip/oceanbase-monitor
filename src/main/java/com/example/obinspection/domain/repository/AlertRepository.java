package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.InspectionAlert;
import com.example.obinspection.domain.model.enums.AlertLevel;

import java.util.List;

/**
 * 告警记录仓储接口。
 */
public interface AlertRepository {

    void save(InspectionAlert alert);

    void saveAll(List<InspectionAlert> alerts);

    List<InspectionAlert> findByTaskId(Long taskId);

    List<InspectionAlert> findByLevel(AlertLevel level);

    /**
     * 查询待处理（PENDING）的告警。
     */
    List<InspectionAlert> findPending();
}
