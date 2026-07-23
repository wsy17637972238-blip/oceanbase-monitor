package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.Alert;

import java.util.List;
import java.util.Optional;

/**
 * 告警仓储接口（domain 层）。
 */
public interface AlertRepository {

    void save(Alert alert);

    Optional<Alert> findById(Long alertId);

    List<Alert> findAll();

    List<Alert> findByStatus(String status);
}
