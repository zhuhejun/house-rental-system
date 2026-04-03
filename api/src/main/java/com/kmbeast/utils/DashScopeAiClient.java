package com.kmbeast.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
    public void init() {
        loadLocalSecretsIfNecessary();
        logConfig();
    }

    private void loadLocalSecretsIfNecessary() {
        if (Boolean.TRUE.equals(enabled) && StringUtils.hasText(apiKey)) {
            return;
        }
        Properties properties = loadLocalSecrets();
        if (properties == null || properties.isEmpty()) {
            return;
        }
        if (!Boolean.TRUE.equals(enabled)) {
            String enabledValue = properties.getProperty("ai.dashscope.enabled");
            if (StringUtils.hasText(enabledValue)) {
                enabled = Boolean.parseBoolean(enabledValue.trim());
            }
        }
        if (!StringUtils.hasText(apiKey)) {
            apiKey = properties.getProperty("ai.dashscope.api-key", apiKey);
        }
        if (!StringUtils.hasText(baseUrl) || "https://dashscope.aliyuncs.com/compatible-mode/v1".equals(baseUrl)) {
            baseUrl = properties.getProperty("ai.dashscope.base-url", baseUrl);
        }
        if (!StringUtils.hasText(model) || "qwen-plus".equals(model)) {
            model = properties.getProperty("ai.dashscope.model", model);
        }
        String temperatureValue = properties.getProperty("ai.dashscope.temperature");
        if (StringUtils.hasText(temperatureValue)) {
            try {
                temperature = Double.parseDouble(temperatureValue.trim());
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private Properties loadLocalSecrets() {
        List<Resource> resources = new ArrayList<>();
        for (File file : resolveCandidateFiles()) {
            if (file.exists() && file.isFile()) {
                resources.add(new FileSystemResource(file));
            }
        }
        if (resources.isEmpty()) {
            return null;
        }
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(resources.toArray(new Resource[0]));
        return yaml.getObject();
    }

    private List<File> resolveCandidateFiles() {
        List<File> files = new ArrayList<>();
        String userDir = System.getProperty("user.dir");
        if (StringUtils.hasText(userDir)) {
            files.add(new File(userDir, "local-secrets.yml"));
            files.add(new File(userDir, "api\\local-secrets.yml"));
            File parent = new File(userDir).getParentFile();
            if (parent != null) {
                files.add(new File(parent, "api\\local-secrets.yml"));
            }
        }
        return files;
    }

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
