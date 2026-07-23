package com.example.obinspection.domain.model;

import java.time.LocalDateTime;

/**
 * 系统配置实体，对应 system_config。
 * 完全遵循 3NF。
 */
public class SystemConfig {

    /** 配置键，主键 */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 说明 */
    private String description;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
