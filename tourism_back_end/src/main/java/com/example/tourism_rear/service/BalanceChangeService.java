package com.example.tourism_rear.service;

import com.example.tourism_rear.pack.utils.BalanceChangeRecord;

import java.util.List;

public interface BalanceChangeService {
    Long recordChange(Integer uid,
                      String changeType,
                      String reason,
                      Double changeAmount,
                      Double beforeBalance,
                      Double afterBalance,
                      Integer status);

    List<BalanceChangeRecord> listByUid(Integer uid);

    int countPendingWithdrawRequest(Integer uid);
}

