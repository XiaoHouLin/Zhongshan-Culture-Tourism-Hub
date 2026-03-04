package com.example.tourism_rear.service;

import com.example.tourism_rear.pack.utils.WithdrawRequestRecord;

import java.util.List;

public interface WithdrawRequestService {
    Long createRequest(Integer uid, Double amount, String phone);

    int countTodayByUid(Integer uid);

    WithdrawRequestRecord getById(Long id);

    WithdrawRequestRecord getLatestPendingByUid(Integer uid);

    List<WithdrawRequestRecord> listAll();

    boolean approve(Long id, Integer reviewUid);

    boolean reject(Long id, Integer reviewUid, String rejectReason);
}
