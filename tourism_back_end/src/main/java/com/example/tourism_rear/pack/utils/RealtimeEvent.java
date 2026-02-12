package com.example.tourism_rear.pack.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeEvent {
    private String type;
    private Integer uid;
    private Integer orderId;
    private Integer orderStatus;
    private String message;
    private Long timestamp;
}
