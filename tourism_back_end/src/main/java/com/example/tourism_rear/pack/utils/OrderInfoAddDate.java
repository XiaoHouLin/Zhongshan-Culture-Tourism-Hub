package com.example.tourism_rear.pack.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
public class OrderInfoAddDate{
    private Integer uid;
    private Integer orderId;
    private String email;
    private String phone;
    private String name;
    private String address;
    private String remark;
    private String agency;
    private LocalDateTime CreateDate;
    private LocalDateTime CompleteDate;
}
