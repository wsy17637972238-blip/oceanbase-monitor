package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.SystemConfig;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置仓储接口（domain 层）。
 */
public interface SystemConfigRepository {

    void save(SystemConfig config);

    Optional<SystemConfig> findById(String key);

    List<SystemConfig> findAll();
}
