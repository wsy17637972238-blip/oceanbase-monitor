package com.example.obinspection.infrastructure.repository;

import com.example.obinspection.domain.model.InspectionRuleConfig;
import com.example.obinspection.domain.repository.InspectionRuleConfigRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class H2InspectionRuleConfigRepository implements InspectionRuleConfigRepository {

    private static final String COLUMNS =
            "rule_id, rule_name, rule_class, category, warn_threshold, critical_threshold, "
                    + "enabled, sort_order, description, updated_at, created_at";

    private static final RowMapper<InspectionRuleConfig> ROW_MAPPER = (rs, rowNum) -> {
        InspectionRuleConfig config = new InspectionRuleConfig();
        config.setRuleId(rs.getString("rule_id"));
        config.setRuleName(rs.getString("rule_name"));
        config.setRuleClass(rs.getString("rule_class"));
        config.setCategory(rs.getString("category"));
        config.setWarnThreshold(rs.getString("warn_threshold"));
        config.setCriticalThreshold(rs.getString("critical_threshold"));
        config.setEnabled(rs.getInt("enabled"));
        config.setSortOrder(rs.getInt("sort_order"));
        config.setDescription(rs.getString("description"));
        config.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        config.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return config;
    };

    private final JdbcTemplate h2JdbcTemplate;

    public H2InspectionRuleConfigRepository(@Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate) {
        this.h2JdbcTemplate = h2JdbcTemplate;
    }

    @Override
    public void save(InspectionRuleConfig config) {
        // TODO: INSERT INTO inspection_rule_config ...
    }

    @Override
    public Optional<InspectionRuleConfig> findById(String ruleId) {
        try {
            return Optional.ofNullable(h2JdbcTemplate.queryForObject(
                    "SELECT " + COLUMNS + " FROM inspection_rule_config WHERE rule_id = ?",
                    ROW_MAPPER, ruleId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<InspectionRuleConfig> findAll() {
        return h2JdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM inspection_rule_config ORDER BY sort_order",
                ROW_MAPPER);
    }

    @Override
    public List<InspectionRuleConfig> findEnabled() {
        return h2JdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM inspection_rule_config WHERE enabled = 1 ORDER BY sort_order",
                ROW_MAPPER);
    }
}
