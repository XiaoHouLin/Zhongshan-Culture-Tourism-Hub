package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.utils.Jwt;
import com.example.tourism_rear.pack.utils.Login;
import com.example.tourism_rear.pack.utils.LoginCaptchaRequest;
import com.example.tourism_rear.pack.utils.LoginCaptchaResponse;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.service.LoginAttemptService;
import com.example.tourism_rear.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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
    @Autowired
    private LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    public Result login(@RequestBody Login user, HttpServletResponse response) {
        log.info("用户登录:{}", user.getUsername());
        String username = user == null ? null : user.getUsername();
        if (loginAttemptService.requiresCaptcha(username)) {
            boolean verified = loginAttemptService.verifyCaptcha(
                    username,
                    user == null ? null : user.getCaptchaToken(),
                    user == null ? null : user.getCaptchaInput()
            );
            if (!verified) {
                Map<String, Object> data = new HashMap<>();
                data.put("needCaptcha", true);
                return new Result(0, "需要人机验证", data);
            }
        }
        if (userService.login(user) != null) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("uid", userService.getUidByUsername(user.getUsername()));
            claims.put("authority", userService.getAuthority(user.getUsername()));
            claims.put("username", user.getUsername());
            String token = Jwt.getToken(claims);
            response.setHeader("Authorization", "Bearer " + token);
            loginAttemptService.reset(user.getUsername());
            return Result.success(token);
        } else {
            int failureCount = loginAttemptService.recordFailure(username);
            if (loginAttemptService.requiresCaptcha(username)) {
                Map<String, Object> data = new HashMap<>();
                data.put("needCaptcha", true);
                data.put("failCount", failureCount);
                return new Result(0, "用户名或密码错误", data);
            }
            return Result.error("用户名或密码错误");
        }
    }

    @PostMapping("/login/captcha")
    public Result createLoginCaptcha(@RequestBody LoginCaptchaRequest request) {
        String username = request == null ? null : request.getUsername();
        if (!StringUtils.hasText(username)) {
            return Result.error("请输入用户名");
        }
        if (!loginAttemptService.requiresCaptcha(username)) {
            return Result.error("暂不需要人机验证");
        }
        LoginAttemptService.CaptchaChallenge challenge = loginAttemptService.createCaptcha(username);
        if (challenge == null) {
            return Result.error("获取人机验证失败，请稍后重试");
        }
        LoginCaptchaResponse response = new LoginCaptchaResponse(
                challenge.getToken(),
                challenge.getQuestion(),
                challenge.getExpiresInSeconds()
        );
        return Result.success(response);
    }

    @PostMapping("/api/validate-token")
    public Result validateToken(HttpServletRequest request) {
        log.info("验证token请求");
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
