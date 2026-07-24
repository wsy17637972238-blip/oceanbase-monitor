package com.example.obinspection.infrastructure.ai;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 诊断响应解析器（纯 POJO）：从模型输出文本中按固定标记解析
 * 根因分析 / 处理建议 / 风险等级三段，解析失败抛异常由调用方落 FAILED。
 */
public final class DiagnosisResultParser {

    private static final Pattern RISK_LEVEL_PATTERN =
            Pattern.compile("\\b(LOW|MEDIUM|HIGH)\\b", Pattern.CASE_INSENSITIVE);

    private DiagnosisResultParser() {
    }

    /** 解析结果（三段式） */
    public record Parsed(String rootCause, String suggestions, String riskLevel) {
    }

    /**
     * @throws IllegalArgumentException 输出缺少「根因分析：」或「处理建议：」标记，或未给出有效风险等级
     */
    public static Parsed parse(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("模型返回内容为空");
        }
        String rootCause = extractSection(content, DiagnosisPromptBuilder.MARKER_ROOT_CAUSE,
                DiagnosisPromptBuilder.MARKER_SUGGESTIONS);
        String suggestions = extractSection(content, DiagnosisPromptBuilder.MARKER_SUGGESTIONS,
                DiagnosisPromptBuilder.MARKER_RISK_LEVEL);

        Matcher matcher = RISK_LEVEL_PATTERN.matcher(content);
        if (!matcher.find()) {
            throw new IllegalArgumentException("模型输出未包含有效风险等级（LOW/MEDIUM/HIGH）");
        }
        return new Parsed(rootCause, suggestions, matcher.group(1).toUpperCase());
    }

    private static String extractSection(String content, String startMarker, String endMarker) {
        int start = content.indexOf(startMarker);
        if (start < 0) {
            throw new IllegalArgumentException("模型输出缺少「" + startMarker + "」标记");
        }
        start += startMarker.length();
        int end = content.indexOf(endMarker, start);
        String section = (end < 0 ? content.substring(start) : content.substring(start, end)).trim();
        if (section.isEmpty()) {
            throw new IllegalArgumentException("模型输出「" + startMarker + "」内容为空");
        }
        return section;
    }
}
