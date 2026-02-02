package com.example.tourism_rear.config;

import com.example.tourism_rear.pack.utils.Jwt;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtConfig {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expire-seconds:86400}")
    private long expireSeconds;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("未配置 jwt.secret，请在环境变量中设置 JWT_SECRET");
        }
        Jwt.setSignKey(secret);
        Jwt.setExpireSeconds(expireSeconds);
    }
}
