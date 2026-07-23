package com.example.obinspection.infrastructure.repository;

import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class H2AlertRepository implements AlertRepository {

    private final JdbcTemplate h2JdbcTemplate;

    public H2AlertRepository(@Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate) {
        this.h2JdbcTemplate = h2JdbcTemplate;
    }

    @Override
    public void save(Alert alert) {
        // TODO: INSERT INTO inspection_alert ...
    }

    @Override
    public Optional<Alert> findById(Long alertId) {
        // TODO: SELECT * FROM inspection_alert WHERE alert_id = ?
        return Optional.empty();
    }

    @Override
    public List<Alert> findAll() {
        // TODO: SELECT * FROM inspection_alert ORDER BY alert_id DESC
        return Collections.emptyList();
    }

    @Override
    public List<Alert> findByStatus(String status) {
        // TODO: SELECT * FROM inspection_alert WHERE status = ?
        return Collections.emptyList();
    }
}
