package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.utils.Register;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class RegisterController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result register(@RequestBody Register registerInfo)
    {
        log.info("用户注册:{}",registerInfo);
        userService.registerUser(registerInfo);
        return Result.success();
    }

    /**
     * 检测用户名是否重复
     *
     * @author : 🐒
     */
    @PostMapping("/isUnique/{username}")
    public Result isUniqueUsername(@PathVariable String username) {
        log.info("检测{}用户名是否已存在", username);
        if (userService.isUniqueUsername(username)) {
            return Result.success();
        } else {
            return Result.error("用户名已存在");
        }
    }

}
