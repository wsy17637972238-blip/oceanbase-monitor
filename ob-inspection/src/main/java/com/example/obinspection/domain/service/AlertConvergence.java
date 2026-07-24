package com.example.obinspection.domain.service;

import com.example.obinspection.domain.model.Alert;

import java.time.LocalDateTime;

/**
 * 告警收敛策略（纯 POJO）。
 * 同一 item_name 在收敛窗口内已有 PENDING 告警时，抑制新告警生成（由调用方刷新原告警内容）。
 */
public final class AlertConvergence {

    /** 收敛窗口（分钟）在 system_config 表中的配置键 */
    public static final String CONFIG_KEY = "alert.convergence.minutes";

    /** 配置缺失或非法时的默认窗口（分钟） */
    public static final int DEFAULT_WINDOW_MINUTES = 5;

    private AlertConvergence() {
    }

    /**
     * 是否抑制新告警：存在窗口内（created_at >= now - window）的 PENDING 告警。
     */
    public static boolean shouldSuppress(Alert existingPending, LocalDateTime now, int windowMinutes) {
        if (existingPending == null || existingPending.getCreatedAt() == null) {
            return false;
        }
        return !existingPending.getCreatedAt().isBefore(now.minusMinutes(windowMinutes));
    }

    /** 解析窗口配置，非法值回退默认窗口 */
    public static int parseWindowMinutes(String configValue) {
        try {
            int minutes = Integer.parseInt(configValue.trim());
            return minutes > 0 ? minutes : DEFAULT_WINDOW_MINUTES;
        } catch (Exception e) {
            return DEFAULT_WINDOW_MINUTES;
        }
    }
}
