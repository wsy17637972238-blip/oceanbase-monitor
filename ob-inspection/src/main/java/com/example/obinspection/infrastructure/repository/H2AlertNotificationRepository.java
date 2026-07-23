package com.example.obinspection.infrastructure.repository;

import com.example.obinspection.domain.model.AlertNotification;
import com.example.obinspection.domain.repository.AlertNotificationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class H2AlertNotificationRepository implements AlertNotificationRepository {

    private final JdbcTemplate h2JdbcTemplate;

    public H2AlertNotificationRepository(@Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate) {
        this.h2JdbcTemplate = h2JdbcTemplate;
    }

    @Override
    public void save(AlertNotification notification) {
        // TODO: INSERT INTO alert_notification ...
    }

    @Override
    public Optional<AlertNotification> findById(Long notificationId) {
        // TODO: SELECT * FROM alert_notification WHERE notification_id = ?
        return Optional.empty();
    }

    @Override
    public List<AlertNotification> findAll() {
        // TODO: SELECT * FROM alert_notification
        return Collections.emptyList();
    }
}
