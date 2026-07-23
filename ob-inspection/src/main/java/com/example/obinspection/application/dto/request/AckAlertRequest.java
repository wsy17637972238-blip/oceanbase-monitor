package com.example.obinspection.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 告警确认请求。
 */
public class AckAlertRequest {

    @NotBlank(message = "确认人不能为空")
    private String ackedBy;

    public String getAckedBy() {
        return ackedBy;
    }

    public void setAckedBy(String ackedBy) {
        this.ackedBy = ackedBy;
    }
}
