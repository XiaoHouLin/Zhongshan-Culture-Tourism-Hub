package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.pagination.PageBean;
import com.example.tourism_rear.pack.utils.EditUser;
import com.example.tourism_rear.pack.utils.EmailChangeCodeRequest;
import com.example.tourism_rear.pack.utils.Jwt;
import com.example.tourism_rear.pack.utils.PasswordChangeRequest;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.pack.utils.UserInfo;
import com.example.tourism_rear.service.EmailVerificationService;
import com.example.tourism_rear.service.RealtimeService;
import com.example.tourism_rear.service.UserService;
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

import java.util.List;
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
        userService.updateUserInfo(editUser);
        if (isAdmin(request)) {
            realtimeService.broadcastGlobalRefresh();
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
