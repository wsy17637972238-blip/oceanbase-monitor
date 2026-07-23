-- ============================================================================
-- OceanBase 智能巡检系统 - 初始化数据
-- ============================================================================

-- 巡检规则配置
INSERT INTO inspection_rule_config (rule_id, rule_name, rule_class, category, warn_threshold, critical_threshold, sort_order, description) VALUES
('slow_sql', '慢SQL监控', 'com.example.obinspection.domain.rule.impl.SlowSqlRule', 'PERFORMANCE', '5', '20', 1, '过去1小时执行时间超过1秒的SQL数量'),
('active_session', '活跃会话监控', 'com.example.obinspection.domain.rule.impl.ActiveSessionRule', 'PERFORMANCE', '50', '100', 2, '非Sleep状态的会话数'),
('merge_status', '合并状态监控', 'com.example.obinspection.domain.rule.impl.MergeStatusRule', 'AVAILABILITY', 'IDLE', NULL, 3, 'Zone合并状态检查');

-- 系统配置
INSERT INTO system_config (config_key, config_value, description) VALUES
('collector.type', 'jdbc', '采集器类型：jdbc/ocp'),
('deepseek.model', 'deepseek-chat', 'AI模型名称'),
('alert.convergence.minutes', '5', '告警收敛时间（分钟）'),
('data.retention.days', '30', '数据保留天数');
