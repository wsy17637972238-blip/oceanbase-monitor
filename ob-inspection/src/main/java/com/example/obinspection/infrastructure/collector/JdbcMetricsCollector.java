package com.example.obinspection.infrastructure.collector;

import com.example.obinspection.domain.collector.MetricNames;
import com.example.obinspection.domain.collector.MetricsCollector;
import com.example.obinspection.domain.model.InspectionInstance;
import com.example.obinspection.domain.model.Metric;
import com.example.obinspection.domain.repository.InspectionInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 基于 JDBC 直连 OceanBase 的指标采集器。
 *
 * 按 {@code inspection_instance} 表中启用（enabled = 1）的实例循环采集：
 * 每个实例通过 {@link ObInstanceConnectionManager} 建独立连接池（缓存复用），
 * 实例连接失败记 WARN 跳过，不影响其他实例；单个指标采集失败同样只记日志不中断整体。
 *
 * 所有指标的 tags 必带 {@link MetricNames#TAG_INSTANCE_ID} / {@link MetricNames#TAG_INSTANCE_NAME}；
 * 指标名统一使用 {@link MetricNames} 常量（规则侧按同名常量消费）。
 */
@Component
public class JdbcMetricsCollector implements MetricsCollector {

    private static final Logger log = LoggerFactory.getLogger(JdbcMetricsCollector.class);

    /** 慢 SQL 判定阈值：执行耗时超过 1 秒（OB 中时间为微秒） */
    private static final long SLOW_SQL_THRESHOLD_US = 1_000_000L;

    /** 慢 SQL 统计窗口：过去 1 小时，避免对 GV$OB_SQL_AUDIT 全表扫历史数据 */
    private static final String SLOW_SQL_SQL =
            "SELECT COUNT(*) FROM oceanbase.GV$OB_SQL_AUDIT "
                    + "WHERE elapsed_time > " + SLOW_SQL_THRESHOLD_US
                    + " AND request_time > UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 HOUR)) * 1000000";

    private static final String ACTIVE_SESSION_SQL =
            "SELECT COUNT(*) FROM oceanbase.GV$OB_PROCESSLIST WHERE command != 'Sleep'";

    private static final String MERGE_STATUS_SQL =
            "SELECT tenant_id, status, is_error, is_suspended, start_time, last_finish_time "
                    + "FROM oceanbase.CDB_OB_MAJOR_COMPACTION";

    private static final String SERVER_STATUS_SQL =
            "SELECT svr_ip, svr_port, zone, status, start_service_time FROM oceanbase.DBA_OB_SERVERS";

    private static final String SERVER_RESOURCE_SQL =
            "SELECT svr_ip, svr_port, cpu_capacity, cpu_assigned, mem_capacity, mem_assigned, "
                    + "data_disk_capacity, data_disk_in_use, log_disk_capacity, log_disk_in_use "
                    + "FROM oceanbase.GV$OB_SERVERS";

    private static final String MEMSTORE_SQL =
            "SELECT tenant_id, svr_ip, memstore_used, memstore_limit FROM oceanbase.GV$OB_MEMSTORE";

    /** 需要采集基线的关键参数 */
    private static final List<String> BASELINE_PARAMS = List.of(
            "enable_major_freeze", "major_freeze_duty_time", "freeze_trigger_percentage",
            "memory_limit_percentage", "system_memory", "cpu_count");

    private final InspectionInstanceRepository instanceRepository;
    private final ObInstanceConnectionManager connectionManager;

    public JdbcMetricsCollector(InspectionInstanceRepository instanceRepository,
                                ObInstanceConnectionManager connectionManager) {
        this.instanceRepository = instanceRepository;
        this.connectionManager = connectionManager;
    }

    @Override
    public List<Metric> collect() {
        long start = System.currentTimeMillis();
        List<InspectionInstance> instances = instanceRepository.findEnabled();
        if (instances.isEmpty()) {
            log.warn("无启用的被巡检实例，跳过采集");
            return List.of();
        }

        List<Metric> metrics = new ArrayList<>();
        for (InspectionInstance instance : instances) {
            JdbcTemplate jdbcTemplate;
            try {
                jdbcTemplate = connectionManager.getJdbcTemplate(instance);
            } catch (Exception e) {
                log.warn("实例[{}]连接失败，跳过该实例采集：{}", instance.getInstanceName(), e.getMessage());
                continue;
            }
            log.info("开始采集实例[{}]", instance.getInstanceName());
            collectSafely("慢SQL数", metrics, () -> collectSlowSqlCount(jdbcTemplate, instance));
            collectSafely("活跃会话数", metrics, () -> collectActiveSessionCount(jdbcTemplate, instance));
            collectSafely("合并状态", metrics, () -> collectMergeStatus(jdbcTemplate, instance));
            collectSafely("节点状态", metrics, () -> collectServerStatus(jdbcTemplate, instance));
            collectSafely("节点资源", metrics, () -> collectServerResource(jdbcTemplate, instance));
            collectSafely("MemStore水位", metrics, () -> collectMemstore(jdbcTemplate, instance));
            collectSafely("参数基线", metrics, () -> collectParameters(jdbcTemplate, instance));
        }

        log.info("指标采集完成：{} 个实例，共 {} 项指标，耗时 {} ms",
                instances.size(), metrics.size(), System.currentTimeMillis() - start);
        return metrics;
    }

    private void collectSafely(String label, List<Metric> sink, Supplier<List<Metric>> supplier) {
        try {
            List<Metric> collected = supplier.get();
            sink.addAll(collected);
            log.info("采集[{}]成功，{} 项", label, collected.size());
        } catch (Exception e) {
            log.warn("采集[{}]失败，跳过该指标：{}", label, e.getMessage());
        }
    }

    /** 所有指标必带的实例标签 */
    private Map<String, String> instanceTags(InspectionInstance instance) {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put(MetricNames.TAG_INSTANCE_ID, String.valueOf(instance.getInstanceId()));
        tags.put(MetricNames.TAG_INSTANCE_NAME, instance.getInstanceName());
        return tags;
    }

    private List<Metric> collectSlowSqlCount(JdbcTemplate jdbcTemplate, InspectionInstance instance) {
        Long count = jdbcTemplate.queryForObject(SLOW_SQL_SQL, Long.class);
        Metric metric = new Metric(MetricNames.SLOW_SQL_COUNT, String.valueOf(count), "count",
                instanceTags(instance));
        log.info("{}[{}] = {}", MetricNames.SLOW_SQL_COUNT, instance.getInstanceName(), metric.getValue());
        return List.of(metric);
    }

    private List<Metric> collectActiveSessionCount(JdbcTemplate jdbcTemplate, InspectionInstance instance) {
        Long count = jdbcTemplate.queryForObject(ACTIVE_SESSION_SQL, Long.class);
        Metric metric = new Metric(MetricNames.ACTIVE_SESSION_COUNT, String.valueOf(count), "count",
                instanceTags(instance));
        log.info("{}[{}] = {}", MetricNames.ACTIVE_SESSION_COUNT, instance.getInstanceName(), metric.getValue());
        return List.of(metric);
    }

    private List<Metric> collectMergeStatus(JdbcTemplate jdbcTemplate, InspectionInstance instance) {
        List<Metric> metrics = jdbcTemplate.query(MERGE_STATUS_SQL, (rs, rowNum) -> {
            Map<String, String> tags = instanceTags(instance);
            tags.put(MetricNames.TAG_TENANT_ID, rs.getString("tenant_id"));
            tags.put("is_error", rs.getString("is_error"));
            tags.put("is_suspended", rs.getString("is_suspended"));
            tags.put("last_finish_time", String.valueOf(rs.getTimestamp("last_finish_time")));
            return new Metric(MetricNames.MERGE_STATUS, rs.getString("status"), "status", tags);
        });
        metrics.forEach(m -> log.info("{}[{} tenant={}] = {} (is_error={}, is_suspended={})",
                MetricNames.MERGE_STATUS, instance.getInstanceName(),
                m.getTags().get(MetricNames.TAG_TENANT_ID), m.getValue(),
                m.getTags().get("is_error"), m.getTags().get("is_suspended")));
        return metrics;
    }

    private List<Metric> collectServerStatus(JdbcTemplate jdbcTemplate, InspectionInstance instance) {
        List<Metric> metrics = jdbcTemplate.query(SERVER_STATUS_SQL, (rs, rowNum) -> {
            Map<String, String> tags = instanceTags(instance);
            tags.put(MetricNames.TAG_SVR_IP, rs.getString("svr_ip"));
            tags.put("svr_port", rs.getString("svr_port"));
            tags.put("zone", rs.getString("zone"));
            tags.put("start_service_time", String.valueOf(rs.getTimestamp("start_service_time")));
            return new Metric(MetricNames.SERVER_STATUS, rs.getString("status"), "status", tags);
        });
        metrics.forEach(m -> log.info("{}[{} {}:{} zone={}] = {}",
                MetricNames.SERVER_STATUS, instance.getInstanceName(),
                m.getTags().get(MetricNames.TAG_SVR_IP), m.getTags().get("svr_port"),
                m.getTags().get("zone"), m.getValue()));
        return metrics;
    }

    private List<Metric> collectServerResource(JdbcTemplate jdbcTemplate, InspectionInstance instance) {
        List<Metric> metrics = new ArrayList<>();
        jdbcTemplate.query(SERVER_RESOURCE_SQL, rs -> {
            Map<String, String> tags = instanceTags(instance);
            tags.put(MetricNames.TAG_SVR_IP, rs.getString("svr_ip"));
            tags.put("svr_port", rs.getString("svr_port"));

            metrics.add(new Metric(MetricNames.SERVER_CPU_USAGE_PERCENT,
                    percent(rs.getLong("cpu_assigned"), rs.getLong("cpu_capacity")), "%", tags));
            metrics.add(new Metric(MetricNames.SERVER_MEM_USAGE_PERCENT,
                    percent(rs.getLong("mem_assigned"), rs.getLong("mem_capacity")), "%", tags));
            metrics.add(new Metric(MetricNames.SERVER_DATA_DISK_USAGE_PERCENT,
                    percent(rs.getLong("data_disk_in_use"), rs.getLong("data_disk_capacity")), "%", tags));
            metrics.add(new Metric(MetricNames.SERVER_LOG_DISK_USAGE_PERCENT,
                    percent(rs.getLong("log_disk_in_use"), rs.getLong("log_disk_capacity")), "%", tags));

            log.info("server.resource[{} {}:{}] cpu={}% mem={}% data_disk={}% log_disk={}%",
                    instance.getInstanceName(), tags.get(MetricNames.TAG_SVR_IP), tags.get("svr_port"),
                    metrics.get(metrics.size() - 4).getValue(),
                    metrics.get(metrics.size() - 3).getValue(),
                    metrics.get(metrics.size() - 2).getValue(),
                    metrics.get(metrics.size() - 1).getValue());
        });
        return metrics;
    }

    private List<Metric> collectMemstore(JdbcTemplate jdbcTemplate, InspectionInstance instance) {
        List<Metric> metrics = jdbcTemplate.query(MEMSTORE_SQL, (rs, rowNum) -> {
            Map<String, String> tags = instanceTags(instance);
            tags.put(MetricNames.TAG_TENANT_ID, rs.getString("tenant_id"));
            tags.put(MetricNames.TAG_SVR_IP, rs.getString("svr_ip"));
            return new Metric(MetricNames.MEMSTORE_USAGE_PERCENT,
                    percent(rs.getLong("memstore_used"), rs.getLong("memstore_limit")), "%", tags);
        });
        metrics.forEach(m -> log.info("{}[{} tenant={} {}] = {}%",
                MetricNames.MEMSTORE_USAGE_PERCENT, instance.getInstanceName(),
                m.getTags().get(MetricNames.TAG_TENANT_ID),
                m.getTags().get(MetricNames.TAG_SVR_IP), m.getValue()));
        return metrics;
    }

    private List<Metric> collectParameters(JdbcTemplate jdbcTemplate, InspectionInstance instance) {
        String placeholders = String.join(", ", BASELINE_PARAMS.stream().map(p -> "?").toList());
        String sql = "SELECT name, value, svr_ip FROM oceanbase.GV$OB_PARAMETERS WHERE name IN ("
                + placeholders + ") ORDER BY name";
        // GV$OB_PARAMETERS 同一参数可能返回多行，按 (name, svr_ip) 去重
        Map<String, Metric> dedup = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            String name = rs.getString("name");
            String svrIp = rs.getString("svr_ip");
            String value = rs.getString("value");
            dedup.computeIfAbsent(name + "@" + svrIp, k -> {
                Map<String, String> tags = instanceTags(instance);
                tags.put(MetricNames.TAG_SVR_IP, svrIp);
                return new Metric(MetricNames.OB_PARAM_PREFIX + name, value, "value", tags);
            });
        }, BASELINE_PARAMS.toArray());
        List<Metric> metrics = new ArrayList<>(dedup.values());
        metrics.forEach(m -> log.info("{}[{}] = {}", m.getName(), instance.getInstanceName(), m.getValue()));
        return metrics;
    }

    /** 计算百分比（保留两位小数）；分母为 0 时返回 0.00 */
    private String percent(long used, long capacity) {
        if (capacity <= 0) {
            return "0.00";
        }
        return BigDecimal.valueOf(used)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(capacity), 2, RoundingMode.HALF_UP)
                .toPlainString();
    }
}
