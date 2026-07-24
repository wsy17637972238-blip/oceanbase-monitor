package com.example.obinspection.domain.repository;

import com.example.obinspection.domain.model.InspectionInstance;

import java.util.List;

/**
 * 被巡检实例仓储接口（domain 层）。
 */
public interface InspectionInstanceRepository {

    List<InspectionInstance> findAll();

    /**
     * 查询所有启用（enabled = 1）的被巡检实例。
     */
    List<InspectionInstance> findEnabled();
}
