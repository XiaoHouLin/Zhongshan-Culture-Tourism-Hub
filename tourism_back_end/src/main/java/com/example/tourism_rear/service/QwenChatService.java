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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
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

    @Value("${qwen.temp-token-url:https://dashscope.aliyuncs.com/api/v1/tokens}")
    private String tempTokenUrl;

    @Value("${qwen.temp-token-ttl:1800}")
    private int tempTokenTtlSeconds;

    @Value("${qwen.timeout-ms:12000}")
    private int timeoutMs;

    @Value("${qwen.proxy-host:}")
    private String proxyHost;

    @Value("${qwen.proxy-port:0}")
    private int proxyPort;

    private RestTemplate restTemplate;
    private final Object tokenLock = new Object();
    private volatile String tempToken;
    private volatile long tempTokenExpiresAt;

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        if (StringUtils.hasText(proxyHost) && proxyPort > 0) {
            factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost.trim(), proxyPort)));
        }
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
            throw new IllegalStateException("Qwen API Key 未配置，请在 application-secrets.yml 或环境变量中设置");
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

        String normalizedBaseUrl = normalizeUrl(baseUrl);
        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", qwenMessages,
                "temperature", 0.6,
                "top_p", 0.9,
                "stream", false
        );

        String token = getTempToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        Map<?, ?> response;
        try {
            response = restTemplate.postForObject(normalizedBaseUrl, entity, Map.class);
        } catch (ResourceAccessException ex) {
            throw handleAccessException(ex, normalizedBaseUrl);
        }
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

    private String getTempToken() {
        long now = System.currentTimeMillis() / 1000;
        if (StringUtils.hasText(tempToken) && tempTokenExpiresAt > now + 60) {
            return tempToken;
        }
        synchronized (tokenLock) {
            long current = System.currentTimeMillis() / 1000;
            if (StringUtils.hasText(tempToken) && tempTokenExpiresAt > current + 60) {
                return tempToken;
            }
            return fetchTempToken();
        }
    }

    private String fetchTempToken() {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("Qwen API Key 未配置，请在 application-secrets.yml 或环境变量中设置");
        }
        int ttl = tempTokenTtlSeconds;
        if (ttl < 1) {
            ttl = 1;
        } else if (ttl > 1800) {
            ttl = 1800;
        }
        String url = normalizeUrl(tempTokenUrl) + "?expire_in_seconds=" + ttl;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<String> entity = new HttpEntity<>("", headers);
        Map<?, ?> response;
        try {
            response = restTemplate.postForObject(url, entity, Map.class);
        } catch (ResourceAccessException ex) {
            throw handleAccessException(ex, url);
        }
        if (response == null) {
            throw new IllegalStateException("临时令牌返回为空");
        }
        Object tokenObj = response.get("token");
        Object expiresObj = response.get("expires_at");
        String newToken = tokenObj != null ? tokenObj.toString() : "";
        long expiresAt = 0;
        if (expiresObj instanceof Number number) {
            expiresAt = number.longValue();
        } else if (expiresObj != null) {
            try {
                expiresAt = Long.parseLong(expiresObj.toString());
            } catch (NumberFormatException ignored) {
                expiresAt = 0;
            }
        }
        if (!StringUtils.hasText(newToken) || expiresAt <= 0) {
            throw new IllegalStateException("临时令牌响应格式异常");
        }
        tempToken = newToken;
        tempTokenExpiresAt = expiresAt;
        return newToken;
    }

    private String normalizeUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        return url.trim();
    }

    private IllegalStateException handleAccessException(ResourceAccessException ex, String url) {
        String proxyInfo = (StringUtils.hasText(proxyHost) && proxyPort > 0)
                ? proxyHost.trim() + ":" + proxyPort
                : "none";
        Throwable cause = ex.getCause();
        log.error("DashScope request failed. url={}, timeoutMs={}, proxy={}, message={}, cause={}",
                url, timeoutMs, proxyInfo, ex.getMessage(), (cause != null ? cause.getClass().getName() : "null"), ex);
        return handleAccessException(ex);
    }

    private IllegalStateException handleAccessException(ResourceAccessException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof java.net.UnknownHostException) {
            return new IllegalStateException("无法解析 dashscope.aliyuncs.com，请检查网络、DNS 或代理设置");
        }
        return new IllegalStateException("无法连接到 DashScope，请检查网络或代理设置");
    }
}
