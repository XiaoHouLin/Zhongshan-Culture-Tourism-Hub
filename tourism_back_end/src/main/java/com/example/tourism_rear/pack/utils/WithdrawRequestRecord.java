package com.example.tourism_rear.pack.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WithdrawRequestRecord {
    private Long id;
    private Integer uid;
    private String username;
    private String cname;
    private Double amount;
    private String phone;
    private Integer status;
    private String rejectReason;
    private Integer reviewUid;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
