package com.example.obinspection.infrastructure.collector;

import com.example.obinspection.domain.model.InspectionInstance;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 被巡检实例连接管理器：按实例懒建 HikariCP 连接池并缓存复用，
 * 应用关闭时统一释放。新建连接后立即执行 SELECT 1 验证连通性，
 * 连接失败直接抛异常由调用方按实例容错跳过。
 */
@Component
public class ObInstanceConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(ObInstanceConnectionManager.class);

    /** 实例全部使用 OceanBase 驱动（与 pom 中 oceanbase-client 依赖对应） */
    private static final String OB_DRIVER_CLASS = "com.oceanbase.jdbc.Driver";

    private final Map<Long, HikariDataSource> pools = new ConcurrentHashMap<>();
    private final Map<Long, JdbcTemplate> templates = new ConcurrentHashMap<>();

    /**
     * 获取实例对应的 JdbcTemplate（首次访问时建池并验证连通性）。
     *
     * @throws org.springframework.dao.DataAccessException 实例连接失败时抛出
     */
    public JdbcTemplate getJdbcTemplate(InspectionInstance instance) {
        return templates.computeIfAbsent(instance.getInstanceId(), id -> create(instance));
    }

    private JdbcTemplate create(InspectionInstance instance) {
        log.info("为实例[{}]({}) 创建连接池", instance.getInstanceName(), instance.getJdbcUrl());
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(instance.getJdbcUrl());
        config.setUsername(instance.getUsername());
        config.setPassword(instance.getPassword());
        config.setDriverClassName(OB_DRIVER_CLASS);
        config.setPoolName("ob-instance-" + instance.getInstanceId());
        config.setMaximumPoolSize(3);
        config.setConnectionTimeout(5000);
        HikariDataSource dataSource = new HikariDataSource(config);
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            // 建池是懒连接，立即验证连通性，失败则关闭池并抛异常
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            pools.put(instance.getInstanceId(), dataSource);
            return jdbcTemplate;
        } catch (RuntimeException e) {
            dataSource.close();
            throw e;
        }
    }

    @PreDestroy
    public void closeAll() {
        pools.forEach((id, ds) -> {
            log.info("关闭实例连接池 ob-instance-{}", id);
            ds.close();
        });
        pools.clear();
        templates.clear();
    }
}
