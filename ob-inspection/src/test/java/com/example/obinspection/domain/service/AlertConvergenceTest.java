package com.example.obinspection.domain.service;

import com.example.obinspection.domain.model.Alert;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertConvergenceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 24, 12, 0, 0);

    private static Alert pendingAlertCreatedAt(LocalDateTime createdAt) {
        Alert alert = new Alert();
        alert.setStatus(Alert.STATUS_PENDING);
        alert.setCreatedAt(createdAt);
        return alert;
    }

    @Test
    void noExistingPendingAlertDoesNotSuppress() {
        assertFalse(AlertConvergence.shouldSuppress(null, NOW, 5));
    }

    @Test
    void pendingAlertWithinWindowSuppresses() {
        // 3 分钟前的 PENDING 告警，窗口 5 分钟 → 抑制
        assertTrue(AlertConvergence.shouldSuppress(pendingAlertCreatedAt(NOW.minusMinutes(3)), NOW, 5));
    }

    @Test
    void pendingAlertAtWindowEdgeSuppresses() {
        // 恰好 5 分钟前（created_at == now - window，不在窗口之外）→ 抑制
        assertTrue(AlertConvergence.shouldSuppress(pendingAlertCreatedAt(NOW.minusMinutes(5)), NOW, 5));
    }

    @Test
    void pendingAlertOutsideWindowDoesNotSuppress() {
        // 6 分钟前的 PENDING 告警，窗口 5 分钟 → 不抑制
        assertFalse(AlertConvergence.shouldSuppress(pendingAlertCreatedAt(NOW.minusMinutes(6)), NOW, 5));
    }

    @Test
    void parseWindowMinutesFallsBackToDefault() {
        assertEquals(5, AlertConvergence.parseWindowMinutes("5"));
        assertEquals(10, AlertConvergence.parseWindowMinutes("10"));
        assertEquals(AlertConvergence.DEFAULT_WINDOW_MINUTES, AlertConvergence.parseWindowMinutes("abc"));
        assertEquals(AlertConvergence.DEFAULT_WINDOW_MINUTES, AlertConvergence.parseWindowMinutes(null));
        assertEquals(AlertConvergence.DEFAULT_WINDOW_MINUTES, AlertConvergence.parseWindowMinutes("0"));
        assertEquals(AlertConvergence.DEFAULT_WINDOW_MINUTES, AlertConvergence.parseWindowMinutes("-3"));
    }
}
