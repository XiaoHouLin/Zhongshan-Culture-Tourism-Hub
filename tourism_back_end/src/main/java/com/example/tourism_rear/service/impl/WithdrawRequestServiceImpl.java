package com.example.tourism_rear.service.impl;

import com.example.tourism_rear.mapper.UserMapper;
import com.example.tourism_rear.mapper.WithdrawRequestMapper;
import com.example.tourism_rear.pack.utils.WithdrawRequestRecord;
import com.example.tourism_rear.service.BalanceChangeService;
import com.example.tourism_rear.service.MessageService;
import com.example.tourism_rear.service.RealtimeService;
import com.example.tourism_rear.service.WithdrawRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WithdrawRequestServiceImpl implements WithdrawRequestService {

    @Autowired
    private WithdrawRequestMapper withdrawRequestMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BalanceChangeService balanceChangeService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private RealtimeService realtimeService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createRequest(Integer uid, Double amount, String phone) {
        if (uid == null || amount == null || !StringUtils.hasText(phone)) {
            return null;
        }
        WithdrawRequestRecord record = new WithdrawRequestRecord();
        record.setUid(uid);
        record.setAmount(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).doubleValue());
        record.setPhone(phone.trim());
        record.setStatus(0);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        withdrawRequestMapper.insert(record);
        return record.getId();
    }

    @Override
    public int countTodayByUid(Integer uid) {
        if (uid == null) {
            return 0;
        }
        return withdrawRequestMapper.countTodayByUid(uid);
    }

    @Override
    public WithdrawRequestRecord getById(Long id) {
        if (id == null) {
            return null;
        }
        return withdrawRequestMapper.findById(id);
    }

    @Override
    public WithdrawRequestRecord getLatestPendingByUid(Integer uid) {
        if (uid == null) {
            return null;
        }
        return withdrawRequestMapper.findLatestPendingByUid(uid);
    }

    @Override
    public List<WithdrawRequestRecord> listAll() {
        return withdrawRequestMapper.listAll();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean approve(Long id, Integer reviewUid) {
        WithdrawRequestRecord request = withdrawRequestMapper.findById(id);
        if (request == null || request.getStatus() == null || request.getStatus() != 0) {
            return false;
        }
        Integer uid = request.getUid();
        if (uid == null) {
            return false;
        }
        Double balance = userMapper.getBalance(uid);
        BigDecimal balanceBD = BigDecimal.valueOf(balance == null ? 0D : balance);
        BigDecimal amountBD = BigDecimal.valueOf(request.getAmount() == null ? 0D : request.getAmount())
                .setScale(2, RoundingMode.HALF_UP);
        if (amountBD.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (balanceBD.compareTo(amountBD) < 0) {
            return false;
        }
        int updated = withdrawRequestMapper.approve(id, reviewUid, LocalDateTime.now());
        if (updated <= 0) {
            return false;
        }
        BigDecimal newBalance = balanceBD.subtract(amountBD).setScale(2, RoundingMode.HALF_UP);
        userMapper.updateBalance(uid, newBalance.doubleValue());
        balanceChangeService.recordChange(
                uid,
                "WITHDRAW",
                "余额提现，申请单 #" + id,
                amountBD.negate().doubleValue(),
                balanceBD.doubleValue(),
                newBalance.doubleValue(),
                1
        );
        messageService.createMessageIfAbsent(
                uid,
                "WITHDRAW_APPROVED",
                String.valueOf(id),
                "提现申请已通过",
                "你的提现申请已通过，金额 ¥" + amountBD + "，预计很快到账。"
        );
        realtimeService.broadcastGlobalRefresh();
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean reject(Long id, Integer reviewUid, String rejectReason) {
        WithdrawRequestRecord request = withdrawRequestMapper.findById(id);
        if (request == null || request.getStatus() == null || request.getStatus() != 0) {
            return false;
        }
        String reason = StringUtils.hasText(rejectReason) ? rejectReason.trim() : "管理员审核未通过";
        int updated = withdrawRequestMapper.reject(id, reviewUid, reason, LocalDateTime.now());
        if (updated <= 0) {
            return false;
        }
        messageService.createMessageIfAbsent(
                request.getUid(),
                "WITHDRAW_REJECTED",
                String.valueOf(id),
                "提现申请未通过",
                "你的提现申请未通过，原因：" + reason + "。如有疑问请联系管理员。"
        );
        realtimeService.broadcastGlobalRefresh();
        return true;
    }
}
