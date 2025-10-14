package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.utils.Jwt;
import com.example.tourism_rear.pack.utils.Login;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class LoginController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody Login user, HttpServletResponse response) {
        log.info("用户登录:{}", user);
        if (userService.login(user) != null) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("uid", userService.getUidByUsername(user.getUsername()));
            claims.put("authority", userService.getAuthority(user.getUsername()));
            claims.put("username", user.getUsername());
            String token = Jwt.getToken(claims);
            response.setHeader("Authorization", "Bearer " + token);
            return Result.success(token);
        } else {
            return Result.error("用户名或密码错误");
        }
    }

    @PostMapping("/api/validate-token")
    public Result validateToken(HttpServletRequest request) {
        log.info("验证token:{}", request.getHeader("Authorization"));
        String jwt = request.getHeader("Authorization");
        if (jwt != null && jwt.contains("Bearer ")) {
            jwt = jwt.substring(7);
        } else {
            return Result.error("无效的token格式");
        }
        try {
            Jwt.checkToken(jwt);
            return Result.success("token合法");
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage(), e);
            return Result.error("无效的token");
        }
    }
}
