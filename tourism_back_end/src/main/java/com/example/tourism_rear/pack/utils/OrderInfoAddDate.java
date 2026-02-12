package com.example.tourism_rear.pack.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class OrderInfoAddDate{
    private Integer uid;
    private Integer orderId;
    private String email;
    private String phone;
    private String name;
    private String address;
    private String remark;
    private Double totalPrice;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime CreateDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime CompleteDate;
    private String pickupCode;
    private String pickupToken;
    private Integer pickupStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime pickupTime;
    private Integer orderStatus;
    private String refundReason;
    private Integer refundOriginStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime refundRequestTime;
}
