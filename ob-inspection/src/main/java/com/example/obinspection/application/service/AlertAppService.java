package com.example.obinspection.application.service;

import com.example.obinspection.application.dto.request.AckAlertRequest;
import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.AlertNotification;
import com.example.obinspection.domain.repository.AlertNotificationRepository;
import com.example.obinspection.domain.repository.AlertRepository;
import com.example.obinspection.domain.repository.SystemConfigRepository;
import com.example.obinspection.domain.service.AlertConvergence;
import com.example.obinspection.domain.service.IdGenerator;
import com.example.obinspection.infrastructure.notifier.AlertNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 告警应用服务。
 */
@Service
public class AlertAppService {

    private static final Logger log = LoggerFactory.getLogger(AlertAppService.class);

    private final AlertRepository alertRepository;
    private final AlertNotificationRepository notificationRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final AlertNotifier alertNotifier;

    public AlertAppService(AlertRepository alertRepository,
                           AlertNotificationRepository notificationRepository,
                           SystemConfigRepository systemConfigRepository,
                           AlertNotifier alertNotifier) {
        this.alertRepository = alertRepository;
        this.notificationRepository = notificationRepository;
        this.systemConfigRepository = systemConfigRepository;
        this.alertNotifier = alertNotifier;
    }

    /**
     * 告警列表，支持按状态（PENDING/ACKED）/级别（WARN/CRITICAL）过滤。
     */
    public List<Alert> listAlerts(String status, String level) {
        return alertRepository.findAll().stream()
                .filter(a -> !StringUtils.hasText(status) || status.equals(a.getStatus()))
                .filter(a -> !StringUtils.hasText(level) || level.equals(a.getLevel()))
                .toList();
    }

    /**
     * 确认告警：PENDING → ACKED（幂等，已确认的重复确认直接返回）。
     */
    public void ackAlert(Long id, AckAlertRequest req) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("告警不存在: " + id));
        if (Alert.STATUS_ACKED.equals(alert.getStatus())) {
            return;
        }
        alert.setStatus(Alert.STATUS_ACKED);
        alert.setAckedBy(req.getAckedBy());
        alert.setAckedAt(LocalDateTime.now());
        alertRepository.update(alert);
        log.info("告警[{}]已被 {} 确认", id, req.getAckedBy());
    }

    /**
     * 保存告警：收敛判定（同 item 在收敛窗口内已有 PENDING 告警 → 刷新原告警，不重复生成）
     * → 入库 → 通知 → 记录通知结果。
     */
    public void saveAlert(Alert alert) {
        int windowMinutes = systemConfigRepository.findById(AlertConvergence.CONFIG_KEY)
                .map(config -> AlertConvergence.parseWindowMinutes(config.getConfigValue()))
                .orElse(AlertConvergence.DEFAULT_WINDOW_MINUTES);
        LocalDateTime now = LocalDateTime.now();

        Optional<Alert> existing = alertRepository.findLatestByItemNameAndStatus(
                alert.getItemName(), Alert.STATUS_PENDING);
        if (AlertConvergence.shouldSuppress(existing.orElse(null), now, windowMinutes)) {
            Alert suppressed = existing.get();
            suppressed.setLevel(alert.getLevel());
            suppressed.setContent(alert.getContent());
            suppressed.setTaskId(alert.getTaskId());
            suppressed.setResultId(alert.getResultId());
            alertRepository.update(suppressed);
            log.info("告警收敛：item[{}] 在 {} 分钟窗口内已有 PENDING 告警[{}]，刷新内容不重复生成",
                    alert.getItemName(), windowMinutes, suppressed.getAlertId());
            return;
        }

        alert.setAlertId(IdGenerator.nextId());
        alert.setCreatedAt(now);
        alert.setNotified(notify(alert) ? 1 : 0);
        alertRepository.save(alert);
        log.info("告警生成：[{}] {} (alertId={})", alert.getLevel(), alert.getItemName(), alert.getAlertId());
    }

    /**
     * 发送通知并落通知记录；通知失败不影响告警入库。
     *
     * @return 是否通知成功
     */
    private boolean notify(Alert alert) {
        AlertNotification record = new AlertNotification();
        record.setNotificationId(IdGenerator.nextId());
        record.setAlertId(alert.getAlertId());
        record.setChannel("console");
        record.setRecipient("console");
        record.setContent(alert.getContent());
        record.setCreatedAt(LocalDateTime.now());
        try {
            alertNotifier.notify(alert);
            record.setSendStatus("SUCCESS");
            record.setSentAt(LocalDateTime.now());
            return true;
        } catch (Exception e) {
            log.warn("告警[{}]通知失败：{}", alert.getAlertId(), e.getMessage());
            record.setSendStatus("FAILED");
            record.setErrorMsg(e.getMessage());
            return false;
        } finally {
            notificationRepository.save(record);
        }
    }
}
