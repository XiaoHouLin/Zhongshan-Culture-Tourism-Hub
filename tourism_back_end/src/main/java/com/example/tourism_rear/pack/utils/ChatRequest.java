package com.example.tourism_rear.pack.utils;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    private List<ChatMessage> messages;
    private String message;
}
