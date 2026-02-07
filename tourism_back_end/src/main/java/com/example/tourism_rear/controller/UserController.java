package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.pagination.PageBean;
import com.example.tourism_rear.pack.utils.EditUser;
import com.example.tourism_rear.pack.utils.PasswordChangeRequest;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.pack.utils.UserInfo;
import com.example.tourism_rear.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/userInfo")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/insert")
    public Result userInfo(@RequestBody UserInfo userInfo) {
        log.info("添加一个用户：{}", userInfo.getUsername());
        userService.addUserInfo(userInfo);
        return Result.success();
    }

    @DeleteMapping("/{uid}")
    public Result deleteUser(@PathVariable List<Integer> uid) {
        log.info("删除用户，uid={}", uid);
        userService.deleteUser(uid);
        return Result.success();
    }

    @PostMapping("/{uid}")
    public Result multiIsUniqueUsers(@PathVariable String uid) {
        log.info("尝试添加新用户，主键值为{}", uid);
        if (userService.multiIsUniqueUsers(uid)) {
            return Result.success();
        }
        return Result.error("主键重复");
    }

    @GetMapping("/{uid}")
    public Result getChangeUsersInfo(@PathVariable String uid) {
        log.info("回显用户信息，uid={}", uid);
        return Result.success(userService.getChangeUsersInfo(uid));
    }

    @PostMapping("/update")
    public Result updateUserInfo(@RequestBody EditUser editUser) {
        log.info("用户{}信息修改为: {}", editUser.getUid(), editUser);
        userService.updateUserInfo(editUser);
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
                           Double balance) {
        log.info("分页查询用户信息: page={}, size={}, uid={}, username={}", page, pageSize, uid, username);
        PageBean userInfoList = userService.list(page, pageSize, uid, username,
                email, authority, cno, cname, csex, cage,
                caddress, cphone, startDate, endDate, cremark, balance);
        return Result.success(userInfoList);
    }

    @GetMapping("/personal/{uid}")
    public Result personal(@PathVariable Integer uid) {
        log.info("查询个人信息，uid={}", uid);
        return Result.success(userService.getUsersInfo(uid));
    }

    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody PasswordChangeRequest request) {
        Integer uid = request.getUid();
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();
        log.info("用户{}修改密码", uid);
        if (userService.updatePassword(uid, oldPassword, newPassword)) {
            return Result.success();
        }
        return Result.error("原密码错误");
    }
}
