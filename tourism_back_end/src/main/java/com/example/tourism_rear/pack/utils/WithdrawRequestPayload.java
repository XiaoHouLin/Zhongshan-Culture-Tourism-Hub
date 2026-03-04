package com.example.tourism_rear.pack.utils;

import lombok.Data;

@Data
public class WithdrawRequestPayload {
    private Double amount;
    private String phone;
}
