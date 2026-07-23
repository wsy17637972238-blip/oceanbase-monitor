package com.example.obinspection.infrastructure.repository;

import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.repository.InspectionResultRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class H2InspectionResultRepository implements InspectionResultRepository {

    private final JdbcTemplate h2JdbcTemplate;

    public H2InspectionResultRepository(@Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate) {
        this.h2JdbcTemplate = h2JdbcTemplate;
    }

    @Override
    public void save(InspectionResult result) {
        // TODO: INSERT INTO inspection_result ...
    }

    @Override
    public Optional<InspectionResult> findById(Long resultId) {
        // TODO: SELECT * FROM inspection_result WHERE result_id = ?
        return Optional.empty();
    }

    @Override
    public List<InspectionResult> findAll() {
        // TODO: SELECT * FROM inspection_result
        return Collections.emptyList();
    }

    @Override
    public List<InspectionResult> findByTaskId(Long taskId) {
        // TODO: SELECT * FROM inspection_result WHERE task_id = ?
        return Collections.emptyList();
    }
}
