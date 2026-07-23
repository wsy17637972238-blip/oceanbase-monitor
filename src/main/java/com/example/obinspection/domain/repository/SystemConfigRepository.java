package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.SystemConfig;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置仓储接口。
 */
public interface SystemConfigRepository {

    Optional<SystemConfig> findByKey(String configKey);

    List<SystemConfig> findAll();
}
