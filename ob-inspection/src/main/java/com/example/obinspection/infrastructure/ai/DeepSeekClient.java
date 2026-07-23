package com.example.obinspection.infrastructure.ai;

import com.example.obinspection.infrastructure.ai.dto.DeepSeekRequest;
import com.example.obinspection.infrastructure.ai.dto.DeepSeekResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * DeepSeek API 客户端。
 */
@Component
public class DeepSeekClient {

    private final RestTemplate restTemplate;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.model}")
    private String model;

    public DeepSeekClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public DeepSeekResponse diagnose(DeepSeekRequest req) {
        // TODO: 携带 Authorization 调用 DeepSeek API 并解析响应
        return null;
    }
}
