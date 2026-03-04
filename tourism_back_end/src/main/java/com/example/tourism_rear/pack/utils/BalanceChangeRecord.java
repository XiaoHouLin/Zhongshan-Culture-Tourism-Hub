package com.example.tourism_rear.pack.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BalanceChangeRecord {
    private Long id;
    private Integer uid;
    private String changeType;
    private String reason;
    private Double changeAmount;
    private Double beforeBalance;
    private Double afterBalance;
    private Integer status;
    private LocalDateTime createTime;
}

