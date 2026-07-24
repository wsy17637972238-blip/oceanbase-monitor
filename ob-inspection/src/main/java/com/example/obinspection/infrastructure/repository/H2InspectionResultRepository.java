package com.example.obinspection.infrastructure.repository;

import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.repository.InspectionResultRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class H2InspectionResultRepository implements InspectionResultRepository {

    private static final String COLUMNS =
            "result_id, task_id, item_name, item_label, metric_value, threshold, status, "
                    + "detail, rule_id, created_at";

    private static final RowMapper<InspectionResult> ROW_MAPPER = (rs, rowNum) -> {
        InspectionResult result = new InspectionResult();
        result.setResultId(rs.getLong("result_id"));
        result.setTaskId(rs.getLong("task_id"));
        result.setItemName(rs.getString("item_name"));
        result.setItemLabel(rs.getString("item_label"));
        result.setMetricValue(rs.getString("metric_value"));
        result.setThreshold(rs.getString("threshold"));
        result.setStatus(rs.getString("status"));
        result.setDetail(rs.getString("detail"));
        result.setRuleId(rs.getString("rule_id"));
        result.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return result;
    };

    private final JdbcTemplate h2JdbcTemplate;

    public H2InspectionResultRepository(@Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate) {
        this.h2JdbcTemplate = h2JdbcTemplate;
    }

    @Override
    public void save(InspectionResult result) {
        h2JdbcTemplate.update(
                "INSERT INTO inspection_result (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                result.getResultId(), result.getTaskId(), result.getItemName(), result.getItemLabel(),
                result.getMetricValue(), result.getThreshold(), result.getStatus(),
                result.getDetail(), result.getRuleId(), result.getCreatedAt());
    }

    @Override
    public Optional<InspectionResult> findById(Long resultId) {
        try {
            return Optional.ofNullable(h2JdbcTemplate.queryForObject(
                    "SELECT " + COLUMNS + " FROM inspection_result WHERE result_id = ?",
                    ROW_MAPPER, resultId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<InspectionResult> findAll() {
        return h2JdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM inspection_result ORDER BY result_id",
                ROW_MAPPER);
    }

    @Override
    public List<InspectionResult> findByTaskId(Long taskId) {
        return h2JdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM inspection_result WHERE task_id = ? ORDER BY result_id",
                ROW_MAPPER, taskId);
    }
}
