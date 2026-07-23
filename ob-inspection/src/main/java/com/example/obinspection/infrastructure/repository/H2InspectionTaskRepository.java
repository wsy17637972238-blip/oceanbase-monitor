package com.example.obinspection.infrastructure.repository;

import com.example.obinspection.domain.model.InspectionTask;
import com.example.obinspection.domain.repository.InspectionTaskRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class H2InspectionTaskRepository implements InspectionTaskRepository {

    private final JdbcTemplate h2JdbcTemplate;

    public H2InspectionTaskRepository(@Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate) {
        this.h2JdbcTemplate = h2JdbcTemplate;
    }

    @Override
    public void save(InspectionTask task) {
        // TODO: INSERT INTO inspection_task ...
    }

    @Override
    public Optional<InspectionTask> findById(Long taskId) {
        // TODO: SELECT * FROM inspection_task WHERE task_id = ?
        return Optional.empty();
    }

    @Override
    public List<InspectionTask> findAll() {
        // TODO: SELECT * FROM inspection_task ORDER BY task_id DESC
        return Collections.emptyList();
    }
}
