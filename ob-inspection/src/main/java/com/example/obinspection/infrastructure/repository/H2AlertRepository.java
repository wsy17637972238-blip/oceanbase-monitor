package com.example.obinspection.infrastructure.repository;

import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class H2AlertRepository implements AlertRepository {

    private static final String COLUMNS =
            "alert_id, task_id, result_id, level, item_name, content, status, "
                    + "acked_by, acked_at, notified, created_at";

    private static final RowMapper<Alert> ROW_MAPPER = (rs, rowNum) -> {
        Alert alert = new Alert();
        alert.setAlertId(rs.getLong("alert_id"));
        alert.setTaskId(rs.getLong("task_id"));
        alert.setResultId((Long) rs.getObject("result_id"));
        alert.setLevel(rs.getString("level"));
        alert.setItemName(rs.getString("item_name"));
        alert.setContent(rs.getString("content"));
        alert.setStatus(rs.getString("status"));
        alert.setAckedBy(rs.getString("acked_by"));
        alert.setAckedAt(rs.getObject("acked_at", LocalDateTime.class));
        alert.setNotified(rs.getInt("notified"));
        alert.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return alert;
    };

    private final JdbcTemplate h2JdbcTemplate;

    public H2AlertRepository(@Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate) {
        this.h2JdbcTemplate = h2JdbcTemplate;
    }

    @Override
    public void save(Alert alert) {
        h2JdbcTemplate.update(
                "INSERT INTO inspection_alert (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                alert.getAlertId(), alert.getTaskId(), alert.getResultId(), alert.getLevel(),
                alert.getItemName(), alert.getContent(), alert.getStatus(),
                alert.getAckedBy(), alert.getAckedAt(), alert.getNotified(), alert.getCreatedAt());
    }

    @Override
    public void update(Alert alert) {
        h2JdbcTemplate.update(
                "UPDATE inspection_alert SET task_id = ?, result_id = ?, level = ?, content = ?, "
                        + "status = ?, acked_by = ?, acked_at = ?, notified = ? WHERE alert_id = ?",
                alert.getTaskId(), alert.getResultId(), alert.getLevel(), alert.getContent(),
                alert.getStatus(), alert.getAckedBy(), alert.getAckedAt(),
                alert.getNotified(), alert.getAlertId());
    }

    @Override
    public Optional<Alert> findById(Long alertId) {
        try {
            return Optional.ofNullable(h2JdbcTemplate.queryForObject(
                    "SELECT " + COLUMNS + " FROM inspection_alert WHERE alert_id = ?",
                    ROW_MAPPER, alertId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Alert> findAll() {
        return h2JdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM inspection_alert ORDER BY alert_id DESC",
                ROW_MAPPER);
    }

    @Override
    public List<Alert> findByStatus(String status) {
        return h2JdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM inspection_alert WHERE status = ? ORDER BY alert_id DESC",
                ROW_MAPPER, status);
    }

    @Override
    public Optional<Alert> findLatestByItemNameAndStatus(String itemName, String status) {
        try {
            return Optional.ofNullable(h2JdbcTemplate.queryForObject(
                    "SELECT " + COLUMNS + " FROM inspection_alert WHERE item_name = ? AND status = ? "
                            + "ORDER BY alert_id DESC LIMIT 1",
                    ROW_MAPPER, itemName, status));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Alert> findByTaskId(Long taskId) {
        return h2JdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM inspection_alert WHERE task_id = ? ORDER BY alert_id",
                ROW_MAPPER, taskId);
    }
}
