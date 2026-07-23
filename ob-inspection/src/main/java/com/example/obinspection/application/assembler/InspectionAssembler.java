package com.example.obinspection.application.assembler;

import com.example.obinspection.application.dto.response.AiDiagnosisDTO;
import com.example.obinspection.application.dto.response.AlertDTO;
import com.example.obinspection.application.dto.response.InspectionResultDTO;
import com.example.obinspection.domain.model.AiDiagnosis;
import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.InspectionResult;

/**
 * 领域对象 -> DTO 装配器（纯静态方法）。
 */
public final class InspectionAssembler {

    private InspectionAssembler() {
    }

    public static InspectionResultDTO toDTO(InspectionResult result) {
        // TODO: 字段拷贝
        return null;
    }

    public static AlertDTO toDTO(Alert alert) {
        // TODO: 字段拷贝
        return null;
    }

    public static AiDiagnosisDTO toDTO(AiDiagnosis diagnosis) {
        // TODO: 字段拷贝
        return null;
    }
}
