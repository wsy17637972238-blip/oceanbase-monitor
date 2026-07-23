package com.example.obinspection.domain.service;

/**
 * 主键生成器（纯 POJO，简化雪花算法）。
 * 结构：毫秒时间戳（相对自定义纪元）左移 12 位 | 毫秒内自增序列（0~4095）。
 * 单进程内单调递增，应用重启不会与历史数据冲突，无需依赖数据库。
 */
public final class IdGenerator {

    /** 自定义纪元：2024-01-01 00:00:00 UTC */
    private static final long EPOCH = 1704067200000L;

    /** 序列号位数：12 位，每毫秒最多 4096 个 ID */
    private static final long SEQUENCE_MASK = (1L << 12) - 1;

    private static long lastTimestamp = -1L;
    private static long sequence = 0L;

    private IdGenerator() {
    }

    public static synchronized long nextId() {
        long now = System.currentTimeMillis();
        if (now < lastTimestamp) {
            // 时钟回拨容忍：沿用上次时间戳，保证单调
            now = lastTimestamp;
        }
        if (now == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0L) {
                // 当前毫秒序列耗尽，自旋等待下一毫秒
                while (now <= lastTimestamp) {
                    now = System.currentTimeMillis();
                }
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = now;
        return ((now - EPOCH) << 12) | sequence;
    }
}
