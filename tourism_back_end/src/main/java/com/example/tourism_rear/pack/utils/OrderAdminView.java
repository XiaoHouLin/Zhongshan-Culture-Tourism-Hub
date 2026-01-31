package com.example.tourism_rear.pack.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderAdminView {
    private Integer orderId;
    private Integer uid;
    private String username;
    private String phone;
    private String email;
    private String name;
    private String address;
    private String remark;
    private LocalDateTime CreateDate;
    private LocalDateTime CompleteDate;
    private String pickupCode;
    private Integer pickupStatus;
    private LocalDateTime pickupTime;
    private Integer orderStatus;
}
