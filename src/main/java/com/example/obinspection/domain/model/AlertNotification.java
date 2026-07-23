package com.example.obinspection.domain.model;

import java.time.LocalDateTime;

/**
 * 告警通知记录实体，对应 alert_notification。
 * 完全遵循 3NF。
 */
public class AlertNotification {

    /** 主键，Java IdGenerator 生成 */
    private Long notificationId;

    /** 逻辑外键 -> inspection_alert.alert_id */
    private Long alertId;

    /** 通知渠道：CONSOLE/EMAIL/WECHAT */
    private String channel;

    /** 接收人/邮箱/Webhook */
    private String recipient;

    /** 实际发送内容 */
    private String content;

    /** 发送状态：PENDING/SUCCESS/FAILED */
    private String sendStatus;

    /** 发送时间 */
    private LocalDateTime sentAt;

    /** 失败原因 */
    private String errorMsg;

    /** 创建时间 */
    private LocalDateTime createdAt;

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus) {
        this.sendStatus = sendStatus;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
