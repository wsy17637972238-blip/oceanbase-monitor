package com.example.obinspection.infrastructure.notifier;

import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.enums.AlertLevel;
import org.springframework.stereotype.Component;

/**
 * 控制台通知器（骨架阶段的默认实现）。
 */
@Component
public class ConsoleNotifier implements AlertNotifier {

    @Override
    public boolean supports(AlertLevel level) {
        return true;
    }

    @Override
    public void notify(Alert alert) {
        System.out.println("TODO: notify " + alert);
    }
}
