package com.example.tourism_rear.service.impl;

import com.example.tourism_rear.mapper.MessageMapper;
import com.example.tourism_rear.pack.utils.MessageNotice;
import com.example.tourism_rear.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createMessageIfAbsent(Integer uid, String messageType, String refId, String title, String content) {
        if (uid == null || !StringUtils.hasText(messageType) || !StringUtils.hasText(refId)) {
            return;
        }
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            return;
        }
        int existing = messageMapper.countByTypeAndRef(uid, messageType, refId);
        if (existing > 0) {
            return;
        }
        MessageNotice notice = new MessageNotice();
        notice.setUid(uid);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setMessageType(messageType);
        notice.setRefId(refId);
        notice.setIsRead(0);
        notice.setCreateTime(LocalDateTime.now());
        messageMapper.insertMessage(notice);
    }

    @Override
    public List<MessageNotice> listMessages(Integer uid) {
        if (uid == null) {
            return List.of();
        }
        return messageMapper.listByUid(uid);
    }

    @Override
    public void markRead(Integer uid, Long id) {
        if (uid == null || id == null) {
            return;
        }
        messageMapper.markRead(uid, id);
    }

    @Override
    public void deleteMessage(Integer uid, Long id) {
        if (uid == null || id == null) {
            return;
        }
        messageMapper.deleteMessage(uid, id);
    }
}
