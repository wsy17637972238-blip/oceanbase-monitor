package com.example.obinspection.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Demo 查询接口：直接从 OceanBase 查询规则配置与系统配置。
 */
@RestController
@RequestMapping("/api")
public class DemoController {

    private final JdbcTemplate jdbcTemplate;

    public DemoController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 查询所有巡检规则配置。
     */
    @GetMapping("/rules")
    public List<Map<String, Object>> listRules() {
        return jdbcTemplate.queryForList(
                "SELECT rule_id, rule_name, category, warn_threshold, critical_threshold, enabled, sort_order, description "
                        + "FROM inspection_rule_config ORDER BY sort_order");
    }

    /**
     * 查询所有系统配置。
     */
    @GetMapping("/configs")
    public List<Map<String, Object>> listConfigs() {
        return jdbcTemplate.queryForList(
                "SELECT config_key, config_value, description FROM system_config");
    }
}
