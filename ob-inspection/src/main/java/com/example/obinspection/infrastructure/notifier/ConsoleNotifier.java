package com.example.obinspection.infrastructure.notifier;

import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.enums.AlertLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 控制台通知器（默认实现，所有级别都打到日志）。
 */
@Component
public class ConsoleNotifier implements AlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotifier.class);

    @Override
    public boolean supports(AlertLevel level) {
        return true;
    }

    @Override
    public void notify(Alert alert) {
        log.info("【告警通知】[{}] {} | {}", alert.getLevel(), alert.getItemName(), alert.getContent());
    }
}
