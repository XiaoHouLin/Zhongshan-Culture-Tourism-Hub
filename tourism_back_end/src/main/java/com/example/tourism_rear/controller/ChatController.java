package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.utils.ChatMessage;
import com.example.tourism_rear.pack.utils.ChatRequest;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.service.QwenChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class ChatController {

    @Autowired
    private QwenChatService qwenChatService;

    @PostMapping("/chat")
    public Result chat(@RequestBody ChatRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        if (request != null) {
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                messages.addAll(request.getMessages());
            } else if (StringUtils.hasText(request.getMessage())) {
                ChatMessage message = new ChatMessage();
                message.setRole("user");
                message.setContent(request.getMessage());
                messages.add(message);
            }
        }
        if (messages.isEmpty()) {
            return Result.error("请输入问题");
        }
        if (!qwenChatService.isTourismRelated(messages)) {
            Map<String, Object> data = new HashMap<>();
            data.put("reply", "抱歉，我只能回答与中山市旅游相关的问题。");
            data.put("blocked", true);
            return Result.success(data);
        }
        try {
            String reply = qwenChatService.chat(messages);
            Map<String, Object> data = new HashMap<>();
            data.put("reply", reply);
            return Result.success(data);
        } catch (Exception e) {
            log.error("Qwen chat error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
