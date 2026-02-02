package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.utils.EmailCodeRequest;
import com.example.tourism_rear.pack.utils.Register;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.service.EmailVerificationService;
import com.example.tourism_rear.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

@Slf4j
@RestController
public class RegisterController {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$");

    @Autowired
    private UserService userService;
    @Autowired
    private EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public Result register(@RequestBody Register registerInfo)
    {
        log.info("User register request: username={}, email={}", registerInfo.getUsername(), registerInfo.getEmail());
        try {
            validateRegister(registerInfo);
            emailVerificationService.verifyRegisterCode(registerInfo.getEmail(), registerInfo.getEmailCode());
            userService.registerUser(registerInfo);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register/email-code")
    public Result sendRegisterEmailCode(@RequestBody EmailCodeRequest request) {
        try {
            emailVerificationService.sendRegisterCode(request.getEmail(), request.getUsername());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Check if username is unique
     */
    @PostMapping("/isUnique/{username}")
    public Result isUniqueUsername(@PathVariable String username) {
        log.info("Check username unique: {}", username);
        if (userService.isUniqueUsername(username)) {
            return Result.success();
        } else {
            return Result.error("用户名已存在");
        }
    }

    private void validateRegister(Register registerInfo) {
        String username = registerInfo.getUsername();
        String password = registerInfo.getPassword();
        String email = registerInfo.getEmail();

        if (!StringUtils.hasText(username)) {
            throw new RuntimeException("用户名不能为空");
        }
        if (username.length() < 5 || username.length() > 20) {
            throw new RuntimeException("用户名长度需为5-20位");
        }
        if (!StringUtils.hasText(password)) {
            throw new RuntimeException("密码不能为空");
        }
        if (password.length() < 8 || password.length() > 20) {
            throw new RuntimeException("密码长度需为8-20位");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new RuntimeException("密码需包含字母和数字");
        }
        if (!StringUtils.hasText(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new RuntimeException("邮箱格式不正确");
        }
        if (!userService.isUniqueUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
    }
}
