package com.example.obinspection.application.service;

import com.example.obinspection.application.dto.request.AckAlertRequest;
import com.example.obinspection.domain.model.Alert;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 告警应用服务。
 */
@Service
public class AlertAppService {

    public List<Alert> listAlerts() {
        // TODO: 查询告警列表
        return Collections.emptyList();
    }

    public void ackAlert(Long id, AckAlertRequest req) {
        // TODO: 更新告警状态为已确认
    }

    public void saveAlert(Alert alert) {
        // TODO: 保存告警并发布 AlertTriggeredEvent / 触发通知
    }
}
