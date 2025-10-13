package com.example.medicine_rear.pack.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Map;

public class Jwt {
    private final static String signKey = "SunWukong";
    private final static String expire = "86400";   // 过期时间，单位/秒

    public static String getToken(Map<String, Object> claims) {
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, signKey)
                .addClaims(claims)
                .setExpiration(new java.util.Date(new java.util.Date().getTime() + Long.parseLong(expire) * 1000))
                .compact();
    }
    public static Claims checkToken(String token) {
        return Jwts.parser()
                .setSigningKey(signKey)
                .parseClaimsJws(token)
                .getBody();
    }
}
