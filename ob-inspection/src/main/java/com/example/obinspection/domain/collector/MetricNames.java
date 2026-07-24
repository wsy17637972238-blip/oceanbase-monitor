package com.example.obinspection.domain.collector;

/**
 * 指标名与标签键常量。
 *
 * 采集器（infrastructure.collector）产出指标、规则（domain.rule.impl）消费指标，
 * 双方必须统一使用本类常量，禁止散落的字符串字面量——拼写不一致会导致规则静默失效。
 */
public final class MetricNames {

    private MetricNames() {
    }

    // ===== 三条核心规则消费的指标 =====

    /** 过去 1 小时执行耗时超过 1 秒的 SQL 数（SlowSqlRule 消费） */
    public static final String SLOW_SQL_COUNT = "slow_sql.count";

    /** 非 Sleep 状态会话数（ActiveSessionRule 消费） */
    public static final String ACTIVE_SESSION_COUNT = "active_session.count";

    /** 租户合并状态，value 为 IDLE/MERGING 等（MergeStatusRule 消费） */
    public static final String MERGE_STATUS = "merge_status";

    // ===== 节点与资源指标 =====

    /** 节点状态，value 为 ACTIVE 等 */
    public static final String SERVER_STATUS = "server.status";

    /** 节点 CPU 分配占比（assigned / capacity，%） */
    public static final String SERVER_CPU_USAGE_PERCENT = "server.cpu.usage.percent";

    /** 节点内存分配占比（assigned / capacity，%） */
    public static final String SERVER_MEM_USAGE_PERCENT = "server.mem.usage.percent";

    /** 节点数据盘使用占比（%） */
    public static final String SERVER_DATA_DISK_USAGE_PERCENT = "server.data_disk.usage.percent";

    /** 节点日志盘使用占比（%） */
    public static final String SERVER_LOG_DISK_USAGE_PERCENT = "server.log_disk.usage.percent";

    /** 租户 MemStore 水位（used / limit，%） */
    public static final String MEMSTORE_USAGE_PERCENT = "memstore.usage.percent";

    /** 关键参数基线指标前缀，完整指标名为 ob.param.&lt;参数名&gt; */
    public static final String OB_PARAM_PREFIX = "ob.param.";

    // ===== 标签键 =====

    /** 被巡检实例 ID（所有指标必带） */
    public static final String TAG_INSTANCE_ID = "instance_id";

    /** 被巡检实例名称（所有指标必带） */
    public static final String TAG_INSTANCE_NAME = "instance_name";

    /** 租户 ID（租户级指标携带） */
    public static final String TAG_TENANT_ID = "tenant_id";

    /** 节点 IP（节点级指标携带） */
    public static final String TAG_SVR_IP = "svr_ip";
}
