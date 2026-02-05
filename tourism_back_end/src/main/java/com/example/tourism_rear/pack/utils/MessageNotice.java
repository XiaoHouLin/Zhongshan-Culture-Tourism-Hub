package com.example.tourism_rear.pack.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageNotice {
    private Long id;
    private Integer uid;
    private String title;
    private String content;
    private String messageType;
    private String refId;
    private Integer isRead;
    private LocalDateTime createTime;
}
