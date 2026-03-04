package com.example.tourism_rear.pack.utils;

import lombok.Data;

@Data
public class WithdrawReviewPayload {
    private Boolean approved;
    private String rejectReason;
}
