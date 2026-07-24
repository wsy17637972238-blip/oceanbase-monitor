package com.example.obinspection.infrastructure.repository;

import com.example.obinspection.domain.model.AiDiagnosis;
import com.example.obinspection.domain.repository.AiDiagnosisRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class H2AiDiagnosisRepository implements AiDiagnosisRepository {

    private static final String COLUMNS =
            "diagnosis_id, alert_id, task_id, model_name, prompt, raw_response, diagnosis_result, "
                    + "root_cause, suggestions, risk_level, call_status, error_msg, token_used, created_at";

    private static final RowMapper<AiDiagnosis> ROW_MAPPER = (rs, rowNum) -> {
        AiDiagnosis diagnosis = new AiDiagnosis();
        diagnosis.setDiagnosisId(rs.getLong("diagnosis_id"));
        diagnosis.setAlertId(rs.getLong("alert_id"));
        diagnosis.setTaskId(rs.getLong("task_id"));
        diagnosis.setModelName(rs.getString("model_name"));
        diagnosis.setPrompt(rs.getString("prompt"));
        diagnosis.setRawResponse(rs.getString("raw_response"));
        diagnosis.setDiagnosisResult(rs.getString("diagnosis_result"));
        diagnosis.setRootCause(rs.getString("root_cause"));
        diagnosis.setSuggestions(rs.getString("suggestions"));
        diagnosis.setRiskLevel(rs.getString("risk_level"));
        diagnosis.setCallStatus(rs.getString("call_status"));
        diagnosis.setErrorMsg(rs.getString("error_msg"));
        diagnosis.setTokenUsed((Integer) rs.getObject("token_used"));
        diagnosis.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return diagnosis;
    };

    private final JdbcTemplate h2JdbcTemplate;

    public H2AiDiagnosisRepository(@Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate) {
        this.h2JdbcTemplate = h2JdbcTemplate;
    }

    @Override
    public void save(AiDiagnosis diagnosis) {
        h2JdbcTemplate.update(
                "INSERT INTO ai_diagnosis (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                diagnosis.getDiagnosisId(), diagnosis.getAlertId(), diagnosis.getTaskId(),
                diagnosis.getModelName(), diagnosis.getPrompt(), diagnosis.getRawResponse(),
                diagnosis.getDiagnosisResult(), diagnosis.getRootCause(), diagnosis.getSuggestions(),
                diagnosis.getRiskLevel(), diagnosis.getCallStatus(), diagnosis.getErrorMsg(),
                diagnosis.getTokenUsed(), diagnosis.getCreatedAt());
    }

    @Override
    public void update(AiDiagnosis diagnosis) {
        h2JdbcTemplate.update(
                "UPDATE ai_diagnosis SET raw_response = ?, diagnosis_result = ?, root_cause = ?, "
                        + "suggestions = ?, risk_level = ?, call_status = ?, error_msg = ?, token_used = ? "
                        + "WHERE diagnosis_id = ?",
                diagnosis.getRawResponse(), diagnosis.getDiagnosisResult(), diagnosis.getRootCause(),
                diagnosis.getSuggestions(), diagnosis.getRiskLevel(), diagnosis.getCallStatus(),
                diagnosis.getErrorMsg(), diagnosis.getTokenUsed(), diagnosis.getDiagnosisId());
    }

    @Override
    public Optional<AiDiagnosis> findById(Long diagnosisId) {
        try {
            return Optional.ofNullable(h2JdbcTemplate.queryForObject(
                    "SELECT " + COLUMNS + " FROM ai_diagnosis WHERE diagnosis_id = ?",
                    ROW_MAPPER, diagnosisId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<AiDiagnosis> findAll() {
        return h2JdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM ai_diagnosis ORDER BY diagnosis_id DESC",
                ROW_MAPPER);
    }

    @Override
    public Optional<AiDiagnosis> findLatestByAlertId(Long alertId) {
        try {
            return Optional.ofNullable(h2JdbcTemplate.queryForObject(
                    "SELECT " + COLUMNS + " FROM ai_diagnosis WHERE alert_id = ? "
                            + "ORDER BY diagnosis_id DESC LIMIT 1",
                    ROW_MAPPER, alertId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
