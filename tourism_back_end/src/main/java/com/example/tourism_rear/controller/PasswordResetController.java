package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.utils.PasswordResetConfirmRequest;
import com.example.tourism_rear.pack.utils.PasswordResetLookupRequest;
import com.example.tourism_rear.pack.utils.PasswordResetSendRequest;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.service.EmailVerificationService;
import com.example.tourism_rear.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@RestController
public class PasswordResetController {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,20}$");

    @Autowired
    private UserService userService;
    @Autowired
    private EmailVerificationService emailVerificationService;

    @PostMapping("/password-reset/lookup")
    public Result lookup(@RequestBody PasswordResetLookupRequest request) {
        String identifier = request.getIdentifier();
        if (!StringUtils.hasText(identifier)) {
            return Result.error("请输入用户名或邮箱");
        }
        boolean isEmail = EMAIL_PATTERN.matcher(identifier).matches();
        Map<String, Object> data = new HashMap<>();
        if (isEmail) {
            String username = userService.getUsernameByEmail(identifier);
            if (!StringUtils.hasText(username)) {
                return Result.error("用户不存在");
            }
            data.put("mode", "email");
            data.put("maskedEmail", maskEmail(identifier));
            return Result.success(data);
        }
        String email = userService.getEmailByUsername(identifier);
        if (!StringUtils.hasText(email)) {
            return Result.error("用户不存在");
        }
        data.put("mode", "username");
        data.put("maskedEmail", maskEmail(email));
        return Result.success(data);
    }

    @PostMapping("/password-reset/send-code")
    public Result sendCode(@RequestBody PasswordResetSendRequest request) {
        String email = request.getEmail();
        String username = request.getUsername();
        if (!StringUtils.hasText(email)) {
            return Result.error("邮箱不能为空");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return Result.error("邮箱格式不正确");
        }
        if (StringUtils.hasText(username)) {
            String storedEmail = userService.getEmailByUsername(username);
            if (!StringUtils.hasText(storedEmail)) {
                return Result.error("用户不存在");
            }
            if (!email.equalsIgnoreCase(storedEmail)) {
                return Result.error("邮箱信息不匹配");
            }
        } else {
            String storedUsername = userService.getUsernameByEmail(email);
            if (!StringUtils.hasText(storedUsername)) {
                return Result.error("用户不存在");
            }
            username = storedUsername;
        }
        emailVerificationService.sendResetCode(email, username);
        return Result.success();
    }

    @PostMapping("/password-reset/verify-code")
    public Result verifyCode(@RequestBody PasswordResetConfirmRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        if (!StringUtils.hasText(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            return Result.error("邮箱格式不正确");
        }
        if (!StringUtils.hasText(code)) {
            return Result.error("验证码不能为空");
        }
        emailVerificationService.checkResetCode(email, code);
        return Result.success();
    }

    @PostMapping("/password-reset/verify-email")
    public Result verifyEmail(@RequestBody PasswordResetSendRequest request) {
        String username = request.getUsername();
        String email = request.getEmail();
        if (!StringUtils.hasText(username)) {
            return Result.error("用户名不能为空");
        }
        if (!StringUtils.hasText(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            return Result.error("邮箱格式不正确");
        }
        String storedEmail = userService.getEmailByUsername(username);
        if (!StringUtils.hasText(storedEmail)) {
            return Result.error("用户不存在");
        }
        if (!email.equalsIgnoreCase(storedEmail)) {
            return Result.error("邮箱信息不匹配");
        }
        return Result.success();
    }

    @PostMapping("/password-reset/confirm")
    public Result confirm(@RequestBody PasswordResetConfirmRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        String newPassword = request.getNewPassword();
        if (!StringUtils.hasText(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            return Result.error("邮箱格式不正确");
        }
        if (!StringUtils.hasText(code)) {
            return Result.error("验证码不能为空");
        }
        if (!StringUtils.hasText(newPassword)) {
            return Result.error("新密码不能为空");
        }
        if (newPassword.length() < 8 || newPassword.length() > 20) {
            return Result.error("密码长度需为8-20位");
        }
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            return Result.error("密码需包含字母和数字");
        }
        String username = userService.getUsernameByEmail(email);
        if (!StringUtils.hasText(username)) {
            return Result.error("用户不存在");
        }
        emailVerificationService.verifyResetCode(email, code);
        if (!userService.resetPasswordByEmail(email, newPassword)) {
            return Result.error("重置失败，请稍后再试");
        }
        return Result.success();
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "*******";
        }
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        int keep = Math.min(4, local.length());
        String prefix = local.substring(0, keep);
        return prefix + "*******@" + domain;
    }
}
