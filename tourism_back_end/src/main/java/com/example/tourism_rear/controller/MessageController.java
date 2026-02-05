package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.utils.Jwt;
import com.example.tourism_rear.pack.utils.MessageAction;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.service.MessageService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping
    public Result listMessages(HttpServletRequest request) {
        Integer uid = getUidFromRequest(request);
        if (uid == null) {
            return Result.error("未登录");
        }
        return Result.success(messageService.listMessages(uid));
    }

    @PostMapping("/read")
    public Result markRead(@RequestBody MessageAction action, HttpServletRequest request) {
        Integer uid = getUidFromRequest(request);
        if (uid == null) {
            return Result.error("未登录");
        }
        if (action == null || action.getId() == null) {
            return Result.error("消息参数缺失");
        }
        messageService.markRead(uid, action.getId());
        return Result.success();
    }

    @PostMapping("/delete")
    public Result deleteMessage(@RequestBody MessageAction action, HttpServletRequest request) {
        Integer uid = getUidFromRequest(request);
        if (uid == null) {
            return Result.error("未登录");
        }
        if (action == null || action.getId() == null) {
            return Result.error("消息参数缺失");
        }
        messageService.deleteMessage(uid, action.getId());
        return Result.success();
    }

    private Integer getUidFromRequest(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if (jwt == null || !jwt.contains("Bearer ")) {
            return null;
        }
        jwt = jwt.substring(7);
        try {
            Claims claims = Jwt.checkToken(jwt);
            Object uid = claims.get("uid");
            return uid == null ? null : Integer.parseInt(uid.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
