package com.example.obinspection.domain.event;

import com.example.obinspection.domain.model.Alert;

import java.time.LocalDateTime;

/**
 * 告警触发领域事件（纯 POJO，不继承 Spring ApplicationEvent）。
 */
public class AlertTriggeredEvent {

    private final Alert alert;
    private final LocalDateTime occurredAt;

    public AlertTriggeredEvent(Alert alert, LocalDateTime occurredAt) {
        this.alert = alert;
        this.occurredAt = occurredAt;
    }

    public Alert getAlert() {
        return alert;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
