package com.example.tourism_rear.pack.utils;

import lombok.Data;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime CreateDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime CompleteDate;
    private String pickupCode;
    private Integer pickupStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime pickupTime;
    private Integer orderStatus;
}
