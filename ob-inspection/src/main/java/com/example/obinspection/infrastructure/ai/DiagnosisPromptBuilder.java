package com.example.obinspection.infrastructure.ai;

import com.example.obinspection.domain.model.Alert;
import com.example.obinspection.domain.model.Metric;

import java.util.List;

/**
 * AI 诊断 Prompt 构造器（链式 API）。
 */
public class DiagnosisPromptBuilder {

    private Alert alert;
    private List<Metric> metrics;

    public DiagnosisPromptBuilder withAlert(Alert alert) {
        this.alert = alert;
        return this;
    }

    public DiagnosisPromptBuilder withMetrics(List<Metric> metrics) {
        this.metrics = metrics;
        return this;
    }

    public String build() {
        // TODO: 将告警与指标信息拼接为结构化 Prompt
        return "";
    }
}
