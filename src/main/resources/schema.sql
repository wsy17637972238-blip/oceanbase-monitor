-- ============================================================================
-- OceanBase 智能巡检系统 - 数据库表结构
-- 兼容：H2 (MODE=MySQL) / MySQL / OceanBase Oracle 租户
-- 设计原则：
--   1. 主键由应用层生成（Java IdGenerator），数据库不自增
--   2. 日期默认值统一 CURRENT_TIMESTAMP，不用 NOW()/SYSDATE
--   3. 分页在 Java 层实现，SQL 不写 LIMIT/OFFSET/ROWNUM
--   4. 外键不建物理约束，靠代码层保证逻辑外键一致性
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 表 1：inspection_task（巡检任务主表）
-- 反范式说明：overall_status 可由子表 inspection_result 聚合推导，
--             冗余存储是为避免频繁 JOIN 查询，空间换时间。
-- ----------------------------------------------------------------------------
CREATE TABLE inspection_task (
    task_id         BIGINT PRIMARY KEY,                -- Java 生成，无 AUTO_INCREMENT
    task_type       VARCHAR(20) DEFAULT 'SCHEDULED',   -- 触发方式：SCHEDULED定时/MANUAL手动
    overall_status  VARCHAR(20) NOT NULL,              -- 整体状态：OK/WARN/CRITICAL（反范式冗余，见表头注释）
    start_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time        TIMESTAMP,                         -- 结束时间
    duration_ms     INT,                               -- 执行耗时毫秒
    collector_type  VARCHAR(20) DEFAULT 'jdbc',        -- 采集器类型：jdbc/ocp
    error_msg       TEXT,                              -- 异常信息
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_start_time (start_time),
    INDEX idx_overall_status (overall_status)
);

-- ----------------------------------------------------------------------------
-- 表 2：inspection_result（巡检结果明细表）
-- 反范式说明：item_label 可由 item_name 通过 rule_config 映射得到，冗余存储
--             是为前端展示避免 JOIN；status 冗余存储是为方便按状态筛选查询。
-- ----------------------------------------------------------------------------
CREATE TABLE inspection_result (
    result_id       BIGINT PRIMARY KEY,                -- Java 生成
    task_id         BIGINT NOT NULL,                   -- 逻辑外键 -> inspection_task.task_id
    item_name       VARCHAR(50) NOT NULL,              -- 巡检项：slow_sql / active_session / merge_status
    item_label      VARCHAR(100),                      -- 展示名称（反范式冗余，见表头注释）
    metric_value    VARCHAR(100) NOT NULL,             -- 实际指标值（统一字符串存不同单位）
    threshold       VARCHAR(100),                      -- 判定阈值
    status          VARCHAR(20) NOT NULL,              -- OK/WARN/CRITICAL（反范式冗余，见表头注释）
    detail          TEXT,                              -- 详细说明（如慢SQL TOP3）
    rule_id         VARCHAR(50),                       -- 命中规则标识
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task_id (task_id),
    INDEX idx_item_name (item_name),
    INDEX idx_status (status)
);

-- ----------------------------------------------------------------------------
-- 表 3：inspection_alert（告警记录表）
-- 反范式说明：content 可由 item_name + metric_value + threshold 拼接生成，
--             冗余固化是为防止后续规则阈值变更导致历史告警记录变化。
-- ----------------------------------------------------------------------------
CREATE TABLE inspection_alert (
    alert_id        BIGINT PRIMARY KEY,                -- Java 生成
    task_id         BIGINT NOT NULL,                   -- 逻辑外键 -> inspection_task.task_id
    result_id       BIGINT,                            -- 逻辑外键 -> inspection_result.result_id，可为空
    level           VARCHAR(20) NOT NULL,              -- INFO/WARN/CRITICAL
    item_name       VARCHAR(50) NOT NULL,              -- 告警项标识
    content         TEXT NOT NULL,                     -- 告警内容（反范式固化，见表头注释）
    status          VARCHAR(20) DEFAULT 'PENDING',     -- PENDING/ACKED/RESOLVED
    acked_by        VARCHAR(50),                       -- 确认人
    acked_at        TIMESTAMP,                         -- 确认时间
    notified        TINYINT DEFAULT 0,                 -- 是否已通知：0否/1是
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task_id (task_id),
    INDEX idx_level (level),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- ----------------------------------------------------------------------------
-- 表 4：ai_diagnosis（AI 智能诊断结果表）
-- 反范式说明：task_id 存在传递依赖（diagnosis_id -> alert_id -> task_id），
--             保留是为避免与 inspection_task 的多表 JOIN，提升查询性能。
-- ----------------------------------------------------------------------------
CREATE TABLE ai_diagnosis (
    diagnosis_id     BIGINT PRIMARY KEY,               -- Java 生成
    alert_id         BIGINT NOT NULL,                  -- 逻辑外键 -> inspection_alert.alert_id
    task_id          BIGINT NOT NULL,                  -- 逻辑外键 -> inspection_task.task_id（反范式冗余，见表头注释）
    model_name       VARCHAR(50) NOT NULL,             -- deepseek-chat / qwen-turbo
    prompt           TEXT,                             -- 发送给 LLM 的完整 Prompt
    raw_response     TEXT,                             -- LLM 原始返回
    diagnosis_result TEXT,                             -- 结构化诊断结果摘要
    root_cause       TEXT,                             -- 根因分析
    suggestions      TEXT,                             -- 优化建议
    risk_level       VARCHAR(20),                      -- LOW/MEDIUM/HIGH
    call_status      VARCHAR(20) DEFAULT 'SUCCESS',    -- SUCCESS/FAILED/TIMEOUT
    error_msg        TEXT,                             -- 失败原因
    token_used       INT,                              -- 消耗 Token 数
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_alert_id (alert_id),
    INDEX idx_task_id (task_id),
    INDEX idx_risk_level (risk_level)
);

-- ----------------------------------------------------------------------------
-- 表 5：inspection_rule_config（巡检规则配置表）
-- 完全遵循 3NF，无冗余。
-- ----------------------------------------------------------------------------
CREATE TABLE inspection_rule_config (
    rule_id            VARCHAR(50) PRIMARY KEY,        -- 规则标识
    rule_name          VARCHAR(100) NOT NULL,          -- 展示名称
    rule_class         VARCHAR(200) NOT NULL,          -- 实现类全路径
    category           VARCHAR(20) NOT NULL,           -- PERFORMANCE/CAPACITY/AVAILABILITY
    warn_threshold     VARCHAR(50),                    -- WARN 阈值
    critical_threshold VARCHAR(50),                    -- CRITICAL 阈值
    enabled            TINYINT DEFAULT 1,              -- 0禁用/1启用
    sort_order         INT DEFAULT 0,                  -- 排序
    description        TEXT,                           -- 规则说明
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 表 6：alert_notification（告警通知记录表）
-- 完全遵循 3NF。
-- ----------------------------------------------------------------------------
CREATE TABLE alert_notification (
    notification_id  BIGINT PRIMARY KEY,               -- Java 生成
    alert_id         BIGINT NOT NULL,                  -- 逻辑外键 -> inspection_alert.alert_id
    channel          VARCHAR(20) NOT NULL,             -- CONSOLE/EMAIL/WECHAT
    recipient        VARCHAR(200),                     -- 接收人/邮箱/Webhook
    content          TEXT,                             -- 实际发送内容
    send_status      VARCHAR(20) DEFAULT 'PENDING',    -- PENDING/SUCCESS/FAILED
    sent_at          TIMESTAMP,                        -- 发送时间
    error_msg        TEXT,                             -- 失败原因
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_alert_id (alert_id),
    INDEX idx_channel (channel)
);

-- ----------------------------------------------------------------------------
-- 表 7：system_config（系统配置表）
-- 完全遵循 3NF。
-- ----------------------------------------------------------------------------
CREATE TABLE system_config (
    config_key       VARCHAR(100) PRIMARY KEY,         -- 配置键
    config_value     TEXT NOT NULL,                    -- 配置值
    description      VARCHAR(500),                     -- 说明
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
