package com.example.obinspection.infrastructure.repository;

import com.example.obinspection.domain.model.AlertNotification;
import com.example.obinspection.domain.repository.AlertNotificationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class H2AlertNotificationRepository implements AlertNotificationRepository {

    private static final String COLUMNS =
            "notification_id, alert_id, channel, recipient, content, send_status, "
                    + "sent_at, error_msg, created_at";

    private static final RowMapper<AlertNotification> ROW_MAPPER = (rs, rowNum) -> {
        AlertNotification notification = new AlertNotification();
        notification.setNotificationId(rs.getLong("notification_id"));
        notification.setAlertId(rs.getLong("alert_id"));
        notification.setChannel(rs.getString("channel"));
        notification.setRecipient(rs.getString("recipient"));
        notification.setContent(rs.getString("content"));
        notification.setSendStatus(rs.getString("send_status"));
        notification.setSentAt(rs.getObject("sent_at", LocalDateTime.class));
        notification.setErrorMsg(rs.getString("error_msg"));
        notification.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return notification;
    };

    private final JdbcTemplate h2JdbcTemplate;

    public H2AlertNotificationRepository(@Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate) {
        this.h2JdbcTemplate = h2JdbcTemplate;
    }

    @Override
    public void save(AlertNotification notification) {
        h2JdbcTemplate.update(
                "INSERT INTO alert_notification (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                notification.getNotificationId(), notification.getAlertId(), notification.getChannel(),
                notification.getRecipient(), notification.getContent(), notification.getSendStatus(),
                notification.getSentAt(), notification.getErrorMsg(), notification.getCreatedAt());
    }

    @Override
    public Optional<AlertNotification> findById(Long notificationId) {
        try {
            return Optional.ofNullable(h2JdbcTemplate.queryForObject(
                    "SELECT " + COLUMNS + " FROM alert_notification WHERE notification_id = ?",
                    ROW_MAPPER, notificationId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<AlertNotification> findAll() {
        return h2JdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM alert_notification ORDER BY notification_id DESC",
                ROW_MAPPER);
    }
}
