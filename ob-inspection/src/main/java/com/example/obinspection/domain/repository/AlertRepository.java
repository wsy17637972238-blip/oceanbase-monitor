package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.Alert;

import java.util.List;
import java.util.Optional;

/**
 * 告警仓储接口（domain 层）。
 */
public interface AlertRepository {

    void save(Alert alert);

    /**
     * 更新告警（内容刷新 / ack 状态流转共用）。
     */
    void update(Alert alert);

    Optional<Alert> findById(Long alertId);

    List<Alert> findAll();

    List<Alert> findByStatus(String status);

    /**
     * 查询指定巡检项最新一条指定状态的告警（收敛判定用）。
     */
    Optional<Alert> findLatestByItemNameAndStatus(String itemName, String status);

    /**
     * 查询指定任务产生的告警（巡检报告附加 AI 建议用）。
     */
    List<Alert> findByTaskId(Long taskId);
}
