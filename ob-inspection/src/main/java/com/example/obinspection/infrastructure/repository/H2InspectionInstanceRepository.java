package com.example.obinspection.infrastructure.repository;

import com.example.obinspection.domain.model.InspectionInstance;
import com.example.obinspection.domain.repository.InspectionInstanceRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class H2InspectionInstanceRepository implements InspectionInstanceRepository {

    private static final String COLUMNS =
            "instance_id, instance_name, jdbc_url, username, password, enabled, description, created_at";

    private static final RowMapper<InspectionInstance> ROW_MAPPER = (rs, rowNum) -> {
        InspectionInstance instance = new InspectionInstance();
        instance.setInstanceId(rs.getLong("instance_id"));
        instance.setInstanceName(rs.getString("instance_name"));
        instance.setJdbcUrl(rs.getString("jdbc_url"));
        instance.setUsername(rs.getString("username"));
        instance.setPassword(rs.getString("password"));
        instance.setEnabled(rs.getInt("enabled"));
        instance.setDescription(rs.getString("description"));
        instance.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return instance;
    };

    private final JdbcTemplate h2JdbcTemplate;

    public H2InspectionInstanceRepository(@Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate) {
        this.h2JdbcTemplate = h2JdbcTemplate;
    }

    @Override
    public List<InspectionInstance> findAll() {
        return h2JdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM inspection_instance ORDER BY instance_id",
                ROW_MAPPER);
    }

    @Override
    public List<InspectionInstance> findEnabled() {
        return h2JdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM inspection_instance WHERE enabled = 1 ORDER BY instance_id",
                ROW_MAPPER);
    }
}
