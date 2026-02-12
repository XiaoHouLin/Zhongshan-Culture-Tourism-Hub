package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.utils.Jwt;
import com.example.tourism_rear.service.RealtimeService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/events")
public class RealtimeController {
    private final RealtimeService realtimeService;

    public RealtimeController(RealtimeService realtimeService) {
        this.realtimeService = realtimeService;
    }

    @GetMapping("/stream")
    public SseEmitter stream(@RequestParam(required = false) String token, HttpServletRequest request) {
        String jwt = extractToken(request, token);
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "缺少凭证");
        }
        try {
            Claims claims = Jwt.checkToken(jwt);
            Object uidObj = claims.get("uid");
            Object roleObj = claims.get("authority");
            Integer uid = uidObj == null ? null : Integer.parseInt(uidObj.toString());
            boolean isAdmin = roleObj != null && Integer.parseInt(roleObj.toString()) == 1;
            if (uid == null && !isAdmin) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "无权限");
            }
            return realtimeService.register(uid, isAdmin);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "无效凭证");
        }
    }

    private String extractToken(HttpServletRequest request, String tokenParam) {
        if (tokenParam != null && !tokenParam.isBlank()) {
            return tokenParam;
        }
        String jwt = request.getHeader("Authorization");
        if (jwt == null || !jwt.contains("Bearer ")) {
            return null;
        }
        return jwt.substring(7);
    }
}
