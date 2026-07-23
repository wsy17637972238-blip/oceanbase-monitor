package com.example.obinspection.infrastructure.notifier;

import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.enums.AlertLevel;

/**
 * 告警通知器接口（无 Spring 注解）。
 */
public interface AlertNotifier {

    boolean supports(AlertLevel level);

    void notify(Alert alert);
}
