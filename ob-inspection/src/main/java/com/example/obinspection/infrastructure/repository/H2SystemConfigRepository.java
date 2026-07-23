package com.example.obinspection.infrastructure.repository;

import com.example.obinspection.domain.model.SystemConfig;
import com.example.obinspection.domain.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class H2SystemConfigRepository implements SystemConfigRepository {

    private static final String COLUMNS = "config_key, config_value, description, updated_at, created_at";

    private static final RowMapper<SystemConfig> ROW_MAPPER = (rs, rowNum) -> {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(rs.getString("config_key"));
        config.setConfigValue(rs.getString("config_value"));
        config.setDescription(rs.getString("description"));
        config.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        config.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return config;
    };

    private final JdbcTemplate h2JdbcTemplate;

    public H2SystemConfigRepository(@Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate) {
        this.h2JdbcTemplate = h2JdbcTemplate;
    }

    @Override
    public void save(SystemConfig config) {
        // TODO: INSERT INTO system_config ...
    }

    @Override
    public Optional<SystemConfig> findById(String key) {
        try {
            return Optional.ofNullable(h2JdbcTemplate.queryForObject(
                    "SELECT " + COLUMNS + " FROM system_config WHERE config_key = ?",
                    ROW_MAPPER, key));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<SystemConfig> findAll() {
        return h2JdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM system_config",
                ROW_MAPPER);
    }
}
