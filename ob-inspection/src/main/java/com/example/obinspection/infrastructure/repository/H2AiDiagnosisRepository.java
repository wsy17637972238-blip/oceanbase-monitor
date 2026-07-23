package com.example.obinspection.infrastructure.repository;

import com.example.obinspection.domain.model.AiDiagnosis;
import com.example.obinspection.domain.repository.AiDiagnosisRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class H2AiDiagnosisRepository implements AiDiagnosisRepository {

    private final JdbcTemplate h2JdbcTemplate;

    public H2AiDiagnosisRepository(@Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate) {
        this.h2JdbcTemplate = h2JdbcTemplate;
    }

    @Override
    public void save(AiDiagnosis diagnosis) {
        // TODO: INSERT INTO ai_diagnosis ...
    }

    @Override
    public Optional<AiDiagnosis> findById(Long diagnosisId) {
        // TODO: SELECT * FROM ai_diagnosis WHERE diagnosis_id = ?
        return Optional.empty();
    }

    @Override
    public List<AiDiagnosis> findAll() {
        // TODO: SELECT * FROM ai_diagnosis
        return Collections.emptyList();
    }
}
