package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.AlertNotification;

import java.util.List;

/**
 * 告警通知记录仓储接口。
 */
public interface AlertNotificationRepository {

    void save(AlertNotification notification);

    List<AlertNotification> findByAlertId(Long alertId);
}
