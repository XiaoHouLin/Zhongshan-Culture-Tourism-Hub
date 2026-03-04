package com.example.tourism_rear.service.impl;

import com.example.tourism_rear.mapper.BalanceChangeMapper;
import com.example.tourism_rear.pack.utils.BalanceChangeRecord;
import com.example.tourism_rear.service.BalanceChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BalanceChangeServiceImpl implements BalanceChangeService {

    @Autowired
    private BalanceChangeMapper balanceChangeMapper;

    @Override
    public Long recordChange(Integer uid, String changeType, String reason, Double changeAmount,
                             Double beforeBalance, Double afterBalance, Integer status) {
        if (uid == null || !StringUtils.hasText(changeType)) {
            return null;
        }
        BalanceChangeRecord record = new BalanceChangeRecord();
        record.setUid(uid);
        record.setChangeType(changeType.trim());
        record.setReason(StringUtils.hasText(reason) ? reason.trim() : "账户余额变动");
        record.setChangeAmount(changeAmount == null ? 0D : changeAmount);
        record.setBeforeBalance(beforeBalance);
        record.setAfterBalance(afterBalance);
        record.setStatus(status == null ? 1 : status);
        record.setCreateTime(LocalDateTime.now());
        balanceChangeMapper.insert(record);
        return record.getId();
    }

    @Override
    public List<BalanceChangeRecord> listByUid(Integer uid) {
        if (uid == null) {
            return List.of();
        }
        return balanceChangeMapper.listByUid(uid);
    }

    @Override
    public int countPendingWithdrawRequest(Integer uid) {
        if (uid == null) {
            return 0;
        }
        return balanceChangeMapper.countPendingWithdrawRequest(uid);
    }
}

