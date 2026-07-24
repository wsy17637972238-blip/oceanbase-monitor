-- OceanBase 智能巡检系统 DDL (H2, MySQL 兼容模式)
-- 注意：H2 MySQL 兼容模式对 ON UPDATE CURRENT_TIMESTAMP 支持不稳，故不使用该子句。

CREATE TABLE IF NOT EXISTS inspection_task (
    task_id BIGINT PRIMARY KEY,
    task_type VARCHAR(20) DEFAULT 'SCHEDULED',
    overall_status VARCHAR(20) NOT NULL,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    duration_ms INT,
    collector_type VARCHAR(20) DEFAULT 'jdbc',
    error_msg TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS inspection_result (
    result_id BIGINT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    item_name VARCHAR(50) NOT NULL,
    item_label VARCHAR(100),
    metric_value VARCHAR(100) NOT NULL,
    threshold VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    detail TEXT,
    rule_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS inspection_alert (
    alert_id BIGINT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    result_id BIGINT,
    level VARCHAR(20) NOT NULL,
    item_name VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    acked_by VARCHAR(50),
    acked_at TIMESTAMP,
    notified TINYINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS ai_diagnosis (
    diagnosis_id BIGINT PRIMARY KEY,
    alert_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    model_name VARCHAR(50) NOT NULL,
    prompt TEXT,
    raw_response TEXT,
    diagnosis_result TEXT,
    root_cause TEXT,
    suggestions TEXT,
    risk_level VARCHAR(20),
    call_status VARCHAR(20) DEFAULT 'SUCCESS',
    error_msg TEXT,
    token_used INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS inspection_rule_config (
    rule_id VARCHAR(50) PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL,
    rule_class VARCHAR(200) NOT NULL,
    category VARCHAR(20) NOT NULL,
    warn_threshold VARCHAR(50),
    critical_threshold VARCHAR(50),
    enabled TINYINT DEFAULT 1,
    sort_order INT DEFAULT 0,
    description TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS alert_notification (
    notification_id BIGINT PRIMARY KEY,
    alert_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(200),
    content TEXT,
    send_status VARCHAR(20) DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    error_msg TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- 被巡检实例纳管表：采集器按 enabled=1 的实例循环采集，指标 tag 带 instance 标识。
-- 注意：password 当前明文存储（仅限本地演示），生产环境必须加密存储，并使用只读最小权限账号。
CREATE TABLE IF NOT EXISTS inspection_instance (
    instance_id BIGINT PRIMARY KEY,
    instance_name VARCHAR(100) NOT NULL,
    jdbc_url VARCHAR(500) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(500),
    enabled TINYINT DEFAULT 1,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS system_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT NOT NULL,
    description VARCHAR(500),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
