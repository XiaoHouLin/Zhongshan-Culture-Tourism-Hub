package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.pagination.PageBean;
import com.example.tourism_rear.pack.utils.EditUser;
import com.example.tourism_rear.pack.utils.EmailChangeCodeRequest;
import com.example.tourism_rear.pack.utils.Jwt;
import com.example.tourism_rear.pack.utils.PasswordChangeRequest;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.pack.utils.UserInfo;
import com.example.tourism_rear.pack.utils.WithdrawRequestDetail;
import com.example.tourism_rear.pack.utils.WithdrawRequestPayload;
import com.example.tourism_rear.pack.utils.WithdrawRequestRecord;
import com.example.tourism_rear.pack.utils.WithdrawReviewPayload;
import com.example.tourism_rear.service.BalanceChangeService;
import com.example.tourism_rear.service.EmailVerificationService;
import com.example.tourism_rear.service.MessageService;
import com.example.tourism_rear.service.RealtimeService;
import com.example.tourism_rear.service.UserService;
import com.example.tourism_rear.service.WithdrawRequestService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/userInfo")
public class UserController {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @Autowired
    private UserService userService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private RealtimeService realtimeService;

    @Autowired
    private BalanceChangeService balanceChangeService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private WithdrawRequestService withdrawRequestService;

    @PostMapping("/insert")
    public Result userInfo(@RequestBody UserInfo userInfo) {
        log.info("Create user: {}", userInfo == null ? null : userInfo.getUsername());
        userService.addUserInfo(userInfo);
        realtimeService.broadcastGlobalRefresh();
        return Result.success();
    }

    @DeleteMapping("/{uid}")
    public Result deleteUser(@PathVariable List<Integer> uid, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限");
        }
        log.info("Delete users: {}", uid);
        if (uid != null) {
            StringBuilder onlineUsers = new StringBuilder();
            for (Integer id : uid) {
                if (id != null && realtimeService.isUserOnline(id)) {
                    if (!onlineUsers.isEmpty()) {
                        onlineUsers.append(",");
                    }
                    onlineUsers.append(id);
                }
            }
            if (!onlineUsers.isEmpty()) {
                return Result.error("用户正在使用中，无法删除：" + onlineUsers);
            }
        }
        userService.deleteUser(uid);
        realtimeService.broadcastGlobalRefresh();
        return Result.success();
    }

    @PostMapping("/{uid}")
    public Result multiIsUniqueUsers(@PathVariable String uid) {
        log.info("Check uid unique: {}", uid);
        if (userService.multiIsUniqueUsers(uid)) {
            return Result.success();
        }
        return Result.error("PRIMARY_KEY_DUPLICATE");
    }

    @GetMapping("/{uid}")
    public Result getChangeUsersInfo(@PathVariable String uid) {
        log.info("Get user detail: {}", uid);
        return Result.success(userService.getChangeUsersInfo(uid));
    }

    @PostMapping("/update")
    public Result updateUserInfo(@RequestBody EditUser editUser, HttpServletRequest request) {
        if (editUser == null || editUser.getUid() == null) {
            return Result.error("USER_INFO_MISSING");
        }
        log.info("Update user: {}, payload: {}", editUser.getUid(), editUser);
        UserInfo current = userService.getUsersInfo(editUser.getUid());
        if (current == null) {
            return Result.error("USER_NOT_FOUND");
        }
        String targetUsername = editUser.getUsername() == null ? null : editUser.getUsername().trim();
        if (!StringUtils.hasText(targetUsername)) {
            return Result.error("USERNAME_REQUIRED");
        }
        if (targetUsername.length() < 5 || targetUsername.length() > 20) {
            return Result.error("USERNAME_LENGTH_INVALID");
        }
        boolean usernameChanged = current.getUsername() == null
                || !targetUsername.equals(current.getUsername());
        if (usernameChanged && !userService.isUniqueUsername(targetUsername)) {
            return Result.error("USERNAME_IN_USE");
        }
        editUser.setUsername(targetUsername);
        String targetEmail = editUser.getEmail();
        if (StringUtils.hasText(targetEmail) && !EMAIL_PATTERN.matcher(targetEmail).matches()) {
            return Result.error("EMAIL_FORMAT_INVALID");
        }
        boolean emailChanged = StringUtils.hasText(targetEmail)
                && (current.getEmail() == null || !targetEmail.equalsIgnoreCase(current.getEmail()));
        if (emailChanged) {
            String existingUser = userService.getUsernameByEmail(targetEmail);
            if (StringUtils.hasText(existingUser)
                    && (current.getUsername() == null || !existingUser.equalsIgnoreCase(current.getUsername()))) {
                return Result.error("EMAIL_IN_USE");
            }
        }
        if (emailChanged && !isAdmin(request)) {
            if (StringUtils.hasText(current.getEmail())) {
                if (!StringUtils.hasText(editUser.getOldEmailCode())) {
                    return Result.error("OLD_EMAIL_REQUIRED");
                }
                emailVerificationService.verifyRegisterCode(current.getEmail(), editUser.getOldEmailCode());
            }
            if (!StringUtils.hasText(editUser.getNewEmailCode())) {
                return Result.error("NEW_EMAIL_REQUIRED");
            }
            emailVerificationService.verifyRegisterCode(targetEmail, editUser.getNewEmailCode());
        }
        boolean adminRequest = isAdmin(request);
        userService.updateUserInfo(editUser);
        if (adminRequest) {
            realtimeService.broadcastGlobalRefresh();
        }
        Integer requestUid = getUidFromRequest(request);
        if (requestUid != null && requestUid.equals(editUser.getUid())) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("uid", editUser.getUid());
            claims.put("authority", userService.getAuthority(editUser.getUsername()));
            claims.put("username", editUser.getUsername());
            Map<String, Object> data = new HashMap<>();
            data.put("token", Jwt.getToken(claims));
            return Result.success(data);
        }
        return Result.success();
    }

    @GetMapping
    public Result userInfo(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "20") Integer pageSize,
                           Integer uid,
                           String username,
                           String email,
                           Integer authority,
                           String cno,
                           String cname,
                           String csex,
                           Integer cage,
                           String caddress,
                           String cphone,
                           String startDate,
                           String endDate,
                           String cremark,
                           Double balance,
                           @RequestParam(required = false) Boolean exactMatch) {
        log.info("List users: page={}, size={}, uid={}, username={}", page, pageSize, uid, username);
        PageBean userInfoList = userService.list(page, pageSize, uid, username,
                email, authority, cno, cname, csex, cage,
                caddress, cphone, startDate, endDate, cremark, balance, exactMatch);
        return Result.success(userInfoList);
    }

    @GetMapping("/personal/{uid}")
    public Result personal(@PathVariable Integer uid) {
        log.info("Get personal info: {}", uid);
        return Result.success(userService.getUsersInfo(uid));
    }

    @GetMapping("/balance/history")
    public Result balanceHistory(HttpServletRequest request) {
        Integer uid = getUidFromRequest(request);
        if (uid == null) {
            return Result.error("无权限");
        }
        return Result.success(balanceChangeService.listByUid(uid));
    }

    @PostMapping("/balance/withdraw/request")
    public Result withdrawRequest(@RequestBody WithdrawRequestPayload payload, HttpServletRequest request) {
        Integer uid = getUidFromRequest(request);
        if (uid == null) {
            return Result.error("无权限");
        }
        Integer authority = getAuthorityFromRequest(request);
        if (authority == null || authority != 3) {
            return Result.error("仅顾客账号支持提现申请");
        }
        if (payload == null || payload.getAmount() == null) {
            return Result.error("提现金额不能为空");
        }
        String phone = payload.getPhone() == null ? "" : payload.getPhone().trim();
        if (!phone.matches("^\\d{11}$")) {
            return Result.error("提现手机号必须为11位数字");
        }
        BigDecimal amount = BigDecimal.valueOf(payload.getAmount()).setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("提现金额必须大于0");
        }
        if (withdrawRequestService.countTodayByUid(uid) > 0) {
            return Result.error("你今天已申请过提现，请明天再试");
        }
        UserInfo userInfo = userService.getUsersInfo(uid);
        if (userInfo == null) {
            return Result.error("用户不存在");
        }
        BigDecimal balance = BigDecimal.valueOf(userInfo.getBalance() == null ? 0D : userInfo.getBalance());
        if (amount.compareTo(balance) > 0) {
            return Result.error("提现金额不能超过账户余额");
        }
        Long requestId = withdrawRequestService.createRequest(uid, amount.doubleValue(), phone);
        if (requestId == null) {
            return Result.error("提现申请提交失败");
        }
        String refId = String.valueOf(requestId);
        String username = userInfo.getUsername() == null ? String.valueOf(uid) : userInfo.getUsername();
        String adminContent = "用户 " + username + "(uid=" + uid + ") 申请提现 ¥" + amount + "，手机号：" + phone + "，请及时处理。";
        List<Integer> adminUids = userService.listAdminUids();
        if (adminUids != null) {
            for (Integer adminUid : adminUids) {
                if (adminUid == null) {
                    continue;
                }
                messageService.createMessageIfAbsent(
                        adminUid,
                        "WITHDRAW_REQUEST",
                        refId,
                        "用户提现申请",
                        adminContent
                );
            }
        }
        messageService.createMessageIfAbsent(
                uid,
                "WITHDRAW_REQUEST_SUBMITTED",
                refId,
                "提现申请已提交",
                "你已提交提现申请，金额 ¥" + amount + "，手机号：" + phone + "。一天仅可申请一次，请等待管理员处理。"
        );
        realtimeService.notifyAdminWithdrawRequest(uid, adminContent);
        return Result.success();
    }

    @GetMapping("/balance/withdraw/today")
    public Result withdrawTodayStatus(HttpServletRequest request) {
        Integer uid = getUidFromRequest(request);
        if (uid == null) {
            return Result.error("无权限");
        }
        int todayCount = withdrawRequestService.countTodayByUid(uid);
        WithdrawRequestRecord pending = withdrawRequestService.getLatestPendingByUid(uid);
        Map<String, Object> data = new HashMap<>();
        data.put("appliedToday", todayCount > 0);
        data.put("pending", pending != null);
        data.put("pendingAmount", pending == null ? null : pending.getAmount());
        data.put("pendingPhone", pending == null ? null : pending.getPhone());
        data.put("requestId", pending == null ? null : pending.getId());
        return Result.success(data);
    }

    @GetMapping("/balance/withdraw/records")
    public Result withdrawRecords(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限");
        }
        return Result.success(withdrawRequestService.listAll());
    }

    @GetMapping("/balance/withdraw/request/{requestId}")
    public Result withdrawRequestDetail(@PathVariable Long requestId, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限");
        }
        WithdrawRequestRecord withdrawRequest = withdrawRequestService.getById(requestId);
        if (withdrawRequest == null) {
            return Result.error("提现申请不存在");
        }
        UserInfo userInfo = userService.getUsersInfo(withdrawRequest.getUid());
        WithdrawRequestDetail detail = new WithdrawRequestDetail();
        detail.setRequest(withdrawRequest);
        detail.setUserInfo(userInfo);
        detail.setBalanceHistory(balanceChangeService.listByUid(withdrawRequest.getUid()));
        return Result.success(detail);
    }

    @PostMapping("/balance/withdraw/request/{requestId}/review")
    public Result reviewWithdrawRequest(@PathVariable Long requestId,
                                        @RequestBody WithdrawReviewPayload payload,
                                        HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限");
        }
        if (payload == null || payload.getApproved() == null) {
            return Result.error("审核参数缺失");
        }
        Integer reviewUid = getUidFromRequest(request);
        boolean success;
        if (Boolean.TRUE.equals(payload.getApproved())) {
            success = withdrawRequestService.approve(requestId, reviewUid);
            if (!success) {
                return Result.error("审核失败，可能申请已处理或余额不足");
            }
            return Result.success();
        }
        success = withdrawRequestService.reject(requestId, reviewUid, payload.getRejectReason());
        if (!success) {
            return Result.error("驳回失败，可能申请已处理");
        }
        return Result.success();
    }

    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody PasswordChangeRequest request) {
        Integer uid = request.getUid();
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();
        log.info("Update password: {}", uid);
        if (userService.updatePassword(uid, oldPassword, newPassword)) {
            return Result.success();
        }
        return Result.error("OLD_PASSWORD_INCORRECT");
    }

    @PostMapping("/self-delete")
    public Result selfDelete(HttpServletRequest request) {
        Integer uid = getUidFromRequest(request);
        if (uid == null) {
            return Result.error("无权限");
        }
        Integer authority = getAuthorityFromRequest(request);
        if (authority == null || authority != 3) {
            return Result.error("仅顾客账号可注销");
        }
        UserInfo userInfo = userService.getUsersInfo(uid);
        if (userInfo == null) {
            return Result.error("用户不存在");
        }
        Double balance = userInfo.getBalance() == null ? 0.0 : userInfo.getBalance();
        if (balance > 0) {
            return Result.error("账户余额不为0，请联系管理员处理");
        }
        userService.deleteUser(List.of(uid));
        realtimeService.broadcastGlobalRefresh();
        return Result.success();
    }

    @PostMapping("/email/old-code")
    public Result sendOldEmailCode(@RequestBody EmailChangeCodeRequest request, HttpServletRequest httpRequest) {
        Integer tokenUid = getUidFromRequest(httpRequest);
        if (tokenUid == null) {
            return Result.error("UNAUTHORIZED");
        }
        Integer uid = request != null && request.getUid() != null ? request.getUid() : tokenUid;
        if (!isAdmin(httpRequest) && !uid.equals(tokenUid)) {
            return Result.error("FORBIDDEN");
        }
        UserInfo info = userService.getUsersInfo(uid);
        if (info == null || !StringUtils.hasText(info.getEmail())) {
            return Result.error("OLD_EMAIL_MISSING");
        }
        emailVerificationService.sendRegisterCode(info.getEmail(), info.getUsername());
        return Result.success();
    }

    @PostMapping("/email/new-code")
    public Result sendNewEmailCode(@RequestBody EmailChangeCodeRequest request, HttpServletRequest httpRequest) {
        Integer tokenUid = getUidFromRequest(httpRequest);
        if (tokenUid == null) {
            return Result.error("UNAUTHORIZED");
        }
        Integer uid = request != null && request.getUid() != null ? request.getUid() : tokenUid;
        if (!isAdmin(httpRequest) && !uid.equals(tokenUid)) {
            return Result.error("FORBIDDEN");
        }
        String email = request == null ? null : request.getEmail();
        if (!StringUtils.hasText(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            return Result.error("EMAIL_FORMAT_INVALID");
        }
        UserInfo info = userService.getUsersInfo(uid);
        if (info != null && StringUtils.hasText(info.getEmail()) && email.equalsIgnoreCase(info.getEmail())) {
            return Result.error("EMAIL_NO_CHANGE");
        }
        String existingUser = userService.getUsernameByEmail(email);
        if (StringUtils.hasText(existingUser)
                && (info == null || info.getUsername() == null || !existingUser.equalsIgnoreCase(info.getUsername()))) {
            return Result.error("EMAIL_IN_USE");
        }
        emailVerificationService.sendRegisterCode(email, info == null ? null : info.getUsername());
        return Result.success();
    }

    private boolean isAdmin(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if (jwt == null || !jwt.contains("Bearer ")) {
            return false;
        }
        jwt = jwt.substring(7);
        try {
            Claims claims = Jwt.checkToken(jwt);
            Object role = claims.get("authority");
            return role != null && Integer.parseInt(role.toString()) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    private Integer getUidFromRequest(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if (jwt == null || !jwt.contains("Bearer ")) {
            return null;
        }
        jwt = jwt.substring(7);
        try {
            Claims claims = Jwt.checkToken(jwt);
            Object uid = claims.get("uid");
            return uid == null ? null : Integer.parseInt(uid.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getAuthorityFromRequest(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if (jwt == null || !jwt.contains("Bearer ")) {
            return null;
        }
        jwt = jwt.substring(7);
        try {
            Claims claims = Jwt.checkToken(jwt);
            Object role = claims.get("authority");
            return role == null ? null : Integer.parseInt(role.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
