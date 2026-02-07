package com.example.tourism_rear.service;

import com.example.tourism_rear.pack.utils.ChatMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
public class QwenChatService {
    private static final Pattern TOPIC_PATTERN = Pattern.compile(
            "(中山市|中山|香山|Zhongshan|孙中山|岐江|孙文西路|古镇|小榄|五桂山|石岐|詹园|紫马岭|翠亨|南朗|菊花会|灯饰|侨乡)");
    private static final String SYSTEM_PROMPT =
            "你是中山市旅游助手，只回答与中山市旅游相关的问题。"
                    + "如果用户问题与中山市旅游无关，请礼貌拒绝，并提示只能回答中山市旅游内容。";

    @Value("${qwen.api-key:}")
    private String apiKey;

    @Value("${qwen.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions}")
    private String baseUrl;

    @Value("${qwen.model:qwen3-max-2026-01-23}")
    private String model;

    @Value("${qwen.timeout-ms:12000}")
    private int timeoutMs;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        this.restTemplate = new RestTemplate(factory);
    }

    public boolean isTourismRelated(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return false;
        }
        StringBuilder builder = new StringBuilder();
        for (ChatMessage message : messages) {
            if (message != null && StringUtils.hasText(message.getContent())) {
                builder.append(message.getContent());
            }
        }
        return TOPIC_PATTERN.matcher(builder.toString()).find();
    }

    public String chat(List<ChatMessage> messages) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("Qwen API Key 未配置，请设置 QWEN_API_KEY 环境变量");
        }
        List<Map<String, String>> qwenMessages = new ArrayList<>();
        qwenMessages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        if (messages != null) {
            for (ChatMessage message : messages) {
                if (message == null || !StringUtils.hasText(message.getContent())) {
                    continue;
                }
                String role = StringUtils.hasText(message.getRole()) ? message.getRole() : "user";
                if (!"user".equals(role) && !"assistant".equals(role) && !"system".equals(role)) {
                    role = "user";
                }
                qwenMessages.add(Map.of("role", role, "content", message.getContent()));
            }
        }

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", qwenMessages,
                "temperature", 0.6,
                "top_p", 0.9,
                "stream", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        Map<?, ?> response = restTemplate.postForObject(baseUrl, entity, Map.class);
        if (response == null) {
            throw new IllegalStateException("模型返回为空");
        }
        Object choicesObj = response.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            throw new IllegalStateException("模型返回格式异常");
        }
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> choiceMap)) {
            throw new IllegalStateException("模型返回格式异常");
        }
        Object messageObj = choiceMap.get("message");
        if (!(messageObj instanceof Map<?, ?> messageMap)) {
            throw new IllegalStateException("模型返回格式异常");
        }
        Object contentObj = messageMap.get("content");
        String content = contentObj != null ? contentObj.toString() : "";
        if (!StringUtils.hasText(content)) {
            throw new IllegalStateException("模型返回为空");
        }
        return content.trim();
    }
}
