package com.example.obinspection.domain.service;

import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.enums.InspectionStatus;

/**
 * 告警生成领域服务（纯 POJO）。
 * 仅 WARN/CRITICAL 的巡检结果生成告警，级别直接映射（WARN→WARN、CRITICAL→CRITICAL）；
 * content 引用结果的判定依据（detail），保证"为什么告警"可追溯。
 */
public class AlertGenerator {

    /**
     * 由巡检结果生成告警；OK 结果返回 null（不告警）。
     * alertId / createdAt 由调用方统一赋值。
     */
    public Alert generateFrom(InspectionResult result) {
        if (result == null || InspectionStatus.OK.name().equals(result.getStatus())) {
            return null;
        }
        Alert alert = new Alert();
        alert.setTaskId(result.getTaskId());
        alert.setResultId(result.getResultId());
        alert.setLevel(result.getStatus());
        alert.setItemName(result.getItemName());
        alert.setContent("[" + result.getStatus() + "] "
                + result.getItemLabel() + "：" + result.getDetail());
        alert.setStatus(Alert.STATUS_PENDING);
        alert.setNotified(0);
        return alert;
    }
}
