package com.example.tourism_rear.pack.utils;

import lombok.Data;

@Data
public class OrderInfo {
    private Integer uid;
    private Integer orderId;
    private String email;
    private String cphone;
    private String cname;
    private String caddress;
    private String cremark;
    private String pickupCode;
    private String pickupToken;
    private Integer pickupStatus;
    private java.time.LocalDateTime pickupTime;
    private Integer orderStatus;
}
