package com.example.tourism_rear.pack.utils;

import lombok.Data;

@Data
public class RefundRequestPayload {
    private Integer orderId;
    private String reason;
}
