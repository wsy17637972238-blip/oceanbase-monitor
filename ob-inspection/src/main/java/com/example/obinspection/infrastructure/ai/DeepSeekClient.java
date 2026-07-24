package com.example.obinspection.infrastructure.ai;

import com.example.obinspection.infrastructure.ai.dto.DeepSeekRequest;
import com.example.obinspection.infrastructure.ai.dto.DeepSeekResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * DeepSeek API 客户端（chat completions）。
 * 调用失败（无 key、网络、超时、HTTP 错误）直接抛异常，由调用方兜底降级。
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

    public String getModel() {
        return model;
    }

    /**
     * 调用 DeepSeek chat completions。model 字段由客户端按配置填充，调用方只需给 messages。
     *
     * @throws IllegalStateException API Key 未配置
     * @throws org.springframework.web.client.RestClientException 网络/超时/HTTP 错误
     */
    public DeepSeekResponse diagnose(DeepSeekRequest req) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("DeepSeek API Key 未配置（环境变量 DEEPSEEK_API_KEY）");
        }
        req.setModel(model);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        DeepSeekResponse response = restTemplate.postForObject(
                apiUrl, new HttpEntity<>(req, headers), DeepSeekResponse.class);
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new IllegalStateException("DeepSeek 返回空响应");
        }
        return response;
    }
}
