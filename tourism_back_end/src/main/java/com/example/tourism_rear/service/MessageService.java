package com.example.tourism_rear.service;

import com.example.tourism_rear.pack.utils.MessageNotice;

import java.util.List;

public interface MessageService {
    void createMessageIfAbsent(Integer uid, String messageType, String refId, String title, String content);

    List<MessageNotice> listMessages(Integer uid);

    void markRead(Integer uid, Long id);

    void deleteMessage(Integer uid, Long id);
}
