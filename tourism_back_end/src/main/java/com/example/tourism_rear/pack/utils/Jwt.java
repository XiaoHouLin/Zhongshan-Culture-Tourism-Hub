package com.example.tourism_rear.pack.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.StringUtils;

import java.util.Map;

public class Jwt {
    private static String signKey = "SunWukong";
    private static long expireSeconds = 86400L;   // 过期时间，单位/秒

    public static void setSignKey(String key) {
        if (StringUtils.hasText(key)) {
            signKey = key;
        }
    }

    public static void setExpireSeconds(long seconds) {
        if (seconds > 0) {
            expireSeconds = seconds;
        }
    }

    public static String getToken(Map<String, Object> claims) {
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, signKey)
                .addClaims(claims)
                .setExpiration(new java.util.Date(new java.util.Date().getTime() + expireSeconds * 1000))
                .compact();
    }
    public static Claims checkToken(String token) {
        return Jwts.parser()
                .setSigningKey(signKey)
                .parseClaimsJws(token)
                .getBody();
    }
}
