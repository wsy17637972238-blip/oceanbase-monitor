package com.example.obinspection.domain.service;

import com.example.obinspection.domain.model.InspectionResult;
import com.example.obinspection.domain.model.enums.InspectionStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * 健康评分计算器（纯 POJO）。
 *
 * 计分规则（简单可解释）：
 * <ul>
 *   <li>基准分 100 分；</li>
 *   <li>每个 WARN 巡检项扣 10 分；</li>
 *   <li>每个 CRITICAL 巡检项扣 30 分；</li>
 *   <li>下限 0 分，不产生负分。</li>
 * </ul>
 * 扣分明细通过 {@link Score#explanation()} 输出，报告中逐条列明，保证"分数怎么算的"可追溯。
 */
public final class HealthScoreCalculator {

    public static final int BASE_SCORE = 100;
    public static final int WARN_DEDUCTION = 10;
    public static final int CRITICAL_DEDUCTION = 30;

    private HealthScoreCalculator() {
    }

    /**
     * 评分结果：分数 + 计分说明。
     */
    public record Score(int value, String explanation) {
    }

    public static Score calculate(List<InspectionResult> results) {
        int score = BASE_SCORE;
        int warnCount = 0;
        int criticalCount = 0;
        List<String> deductionLines = new ArrayList<>();

        for (InspectionResult result : results) {
            if (InspectionStatus.WARN.name().equals(result.getStatus())) {
                warnCount++;
                score -= WARN_DEDUCTION;
                deductionLines.add(String.format("WARN 项「%s」扣 %d 分", result.getItemName(), WARN_DEDUCTION));
            } else if (InspectionStatus.CRITICAL.name().equals(result.getStatus())) {
                criticalCount++;
                score -= CRITICAL_DEDUCTION;
                deductionLines.add(String.format("CRITICAL 项「%s」扣 %d 分", result.getItemName(), CRITICAL_DEDUCTION));
            }
        }
        score = Math.max(score, 0);

        StringBuilder explanation = new StringBuilder();
        explanation.append(String.format("基准分 %d 分；共 %d 个巡检项，其中 WARN %d 项（每项扣 %d 分）、CRITICAL %d 项（每项扣 %d 分）。",
                BASE_SCORE, results.size(), warnCount, WARN_DEDUCTION, criticalCount, CRITICAL_DEDUCTION));
        if (deductionLines.isEmpty()) {
            explanation.append("无扣分项，得满分。");
        } else {
            explanation.append("扣分明细：").append(String.join("；", deductionLines)).append("。");
        }
        explanation.append(String.format("最终得分 %d 分。", score));
        return new Score(score, explanation.toString());
    }
}
