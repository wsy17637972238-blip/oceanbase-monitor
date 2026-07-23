package com.example.obinspection.domain.service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 主键生成器。
 * 演示阶段使用 AtomicLong 自增；生产环境可替换为雪花算法等分布式 ID 方案。
 * 数据库主键统一由应用层生成，不使用自增列。
 */
public final class IdGenerator {

    private static final AtomicLong COUNTER = new AtomicLong(0L);

    private IdGenerator() {
    }

    public static long nextId() {
        return COUNTER.incrementAndGet();
    }
}
