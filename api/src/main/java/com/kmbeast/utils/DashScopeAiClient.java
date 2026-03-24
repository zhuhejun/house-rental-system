package com.kmbeast.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

/**
 * 千问 DashScope OpenAI 兼容模式客户端
 */
@Component
public class DashScopeAiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.dashscope.enabled:false}")
    private Boolean enabled;

    @Value("${ai.dashscope.api-key:}")
    private String apiKey;

    @Value("${ai.dashscope.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${ai.dashscope.model:qwen-plus}")
    private String model;

    @Value("${ai.dashscope.temperature:0.3}")
    private Double temperature;

    @PostConstruct
    public void logConfig() {
        System.out.println("[AI] DashScope enabled=" + enabled
                + ", baseUrl=" + baseUrl
                + ", model=" + model
                + ", hasApiKey=" + StringUtils.hasText(apiKey));
    }

    public boolean isConfigured() {
        return Boolean.TRUE.equals(enabled) && StringUtils.hasText(apiKey) && StringUtils.hasText(baseUrl);
    }

    public String chat(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            return null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("temperature", temperature == null ? 0.3D : temperature);

        JSONArray messages = new JSONArray();
        messages.add(buildMessage("system", systemPrompt));
        messages.add(buildMessage("user", userPrompt));
        body.put("messages", messages);

        HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(buildChatUrl(), entity, String.class);
        return parseContent(response.getBody());
    }

    private JSONObject buildMessage(String role, String content) {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String parseContent(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return null;
        }
        JSONObject response = JSONObject.parseObject(responseBody);
        if (response == null) {
            return null;
        }
        JSONArray choices = response.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        JSONObject choice = choices.getJSONObject(0);
        if (choice == null) {
            return null;
        }
        JSONObject message = choice.getJSONObject("message");
        if (message == null) {
            return null;
        }
        Object content = message.get("content");
        if (content instanceof String) {
            return (String) content;
        }
        if (content instanceof JSONArray) {
            JSONArray array = (JSONArray) content;
            StringBuilder builder = new StringBuilder();
            array.forEach(item -> {
                if (item instanceof JSONObject) {
                    JSONObject object = (JSONObject) item;
                    String text = object.getString("text");
                    if (StringUtils.hasText(text)) {
                        if (builder.length() > 0) {
                            builder.append("\n");
                        }
                        builder.append(text);
                    }
                }
            });
            return builder.toString();
        }
        return null;
    }

    private String buildChatUrl() {
        String normalizedBaseUrl = baseUrl.trim();
        if (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }
        return normalizedBaseUrl + "/chat/completions";
    }
}
