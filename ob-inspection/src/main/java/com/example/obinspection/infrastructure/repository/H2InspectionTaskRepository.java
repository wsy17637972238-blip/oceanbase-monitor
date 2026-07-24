package com.example.obinspection.infrastructure.repository;

import com.example.obinspection.domain.model.InspectionTask;
import com.example.obinspection.domain.repository.InspectionTaskRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class H2InspectionTaskRepository implements InspectionTaskRepository {

    private static final String COLUMNS =
            "task_id, task_type, overall_status, start_time, end_time, duration_ms, "
                    + "collector_type, error_msg, created_at";

    private static final RowMapper<InspectionTask> ROW_MAPPER = (rs, rowNum) -> {
        InspectionTask task = new InspectionTask();
        task.setTaskId(rs.getLong("task_id"));
        task.setTaskType(rs.getString("task_type"));
        task.setOverallStatus(rs.getString("overall_status"));
        task.setStartTime(rs.getObject("start_time", LocalDateTime.class));
        task.setEndTime(rs.getObject("end_time", LocalDateTime.class));
        task.setDurationMs((Integer) rs.getObject("duration_ms"));
        task.setCollectorType(rs.getString("collector_type"));
        task.setErrorMsg(rs.getString("error_msg"));
        task.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return task;
    };

    private final JdbcTemplate h2JdbcTemplate;

    public H2InspectionTaskRepository(@Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate) {
        this.h2JdbcTemplate = h2JdbcTemplate;
    }

    @Override
    public void save(InspectionTask task) {
        h2JdbcTemplate.update(
                "INSERT INTO inspection_task (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                task.getTaskId(), task.getTaskType(), task.getOverallStatus(),
                task.getStartTime(), task.getEndTime(), task.getDurationMs(),
                task.getCollectorType(), task.getErrorMsg(), task.getCreatedAt());
    }

    @Override
    public void update(InspectionTask task) {
        h2JdbcTemplate.update(
                "UPDATE inspection_task SET overall_status = ?, end_time = ?, duration_ms = ?, error_msg = ? "
                        + "WHERE task_id = ?",
                task.getOverallStatus(), task.getEndTime(), task.getDurationMs(),
                task.getErrorMsg(), task.getTaskId());
    }

    @Override
    public Optional<InspectionTask> findById(Long taskId) {
        try {
            return Optional.ofNullable(h2JdbcTemplate.queryForObject(
                    "SELECT " + COLUMNS + " FROM inspection_task WHERE task_id = ?",
                    ROW_MAPPER, taskId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<InspectionTask> findAll() {
        return h2JdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM inspection_task ORDER BY task_id DESC",
                ROW_MAPPER);
    }
}
