package com.example.tourism_rear.pack.utils;

import lombok.Data;

import java.util.List;

@Data
public class WithdrawRequestDetail {
    private WithdrawRequestRecord request;
    private UserInfo userInfo;
    private List<BalanceChangeRecord> balanceHistory;
}
