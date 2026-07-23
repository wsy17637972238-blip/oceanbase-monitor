package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.AlertNotification;

import java.util.List;
import java.util.Optional;

/**
 * 告警通知仓储接口（domain 层）。
 */
public interface AlertNotificationRepository {

    void save(AlertNotification notification);

    Optional<AlertNotification> findById(Long notificationId);

    List<AlertNotification> findAll();
}
