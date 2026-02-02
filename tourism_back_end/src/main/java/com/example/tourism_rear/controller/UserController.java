package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.pagination.PageBean;
import com.example.tourism_rear.pack.utils.EditUser;
import com.example.tourism_rear.pack.utils.PasswordChangeRequest;
import com.example.tourism_rear.service.UserService;
import com.example.tourism_rear.pack.utils.UserInfo;
import com.example.tourism_rear.pack.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/userInfo")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 添加一个用户
     *
     * @author : 🐒
     */
    @PostMapping("/insert")
    public Result userInfo(@RequestBody UserInfo userInfo) {
        log.info("添加一个用户：{}", userInfo.getUsername());
        userService.addUserInfo(userInfo);
        return Result.success();
    }

    /**
     * (批量)删除用户(不可撤销)
     *
     * @author : 🐒
     */
    @DeleteMapping("/{uid}")
    public Result deleteUser(@PathVariable List<Integer> uid) {
        log.info("删除用户，uid为{}", uid);
        userService.deleteUser(uid);
        return Result.success();
    }

    /**
     * 检测主键是否重复，判断与数据库中所有用户主键是否重复
     *
     * @author : 🐒
     */
    @PostMapping("/{uid}")
    public Result multiIsUniqueUsers(@PathVariable String uid) {
        log.info("尝试添加新用户，主键值为{}", uid);
        if (userService.multiIsUniqueUsers(uid)) {
            return Result.success();
        } else {
            return Result.error("主键重复");
        }
    }

    /**
     * 数据回显
     *
     * @return : user表选中的数据
     * @author : 🐒
     */
    @GetMapping("/{uid}")
    public Result getChangeUsersInfo(@PathVariable String uid) {
        log.info("回显用户信息，UID为{}", uid);
        return Result.success(userService.getChangeUsersInfo(uid));
    }


    /**
     * 修改用户信息
     *
     * @author : 🐒
     */
    @PostMapping("/update")
    public Result updateUserInfo(@RequestBody EditUser editUser) {
        log.info("用户{}信息修改为:{}", editUser.getUid(), editUser);
        userService.updateUserInfo(editUser);
        return Result.success();
    }

    /**
     * 查询用户所有信息
     *
     * @return : user表,client表中所有的数据
     * @author : 🐒
     */
    @GetMapping
    public Result userInfo(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "20") Integer pageSize, Integer uid, String username,
                           String email, Integer authority, String cno, String cname, String csex, Integer cage,
                           String caddress, String cphone, String csymptom, String startDate, String endDate, String cremark,
                           Double balance
    ) {
        log.info("分页查询用户信息，页码：{}, 每页数据量：{}, 用户UID:{}, 用户名:{}, 邮箱{}, 权限{}, 顾客编号{}," +
                        "姓名{}, 性别{}, 年龄{}, 地址{}, 电话{}, 症状{}, 起始日期{}, 结束日期{}, 备注{},余额{}",
                page, pageSize, uid, username,
                email, authority, cno, cname, csex, cage,
                caddress, cphone, csymptom, startDate, endDate, cremark, balance);
        PageBean userInfoList = userService.list(page, pageSize, uid, username,
                email, authority, cno, cname, csex, cage,
                caddress, cphone, csymptom, startDate, endDate, cremark, balance);
        return Result.success(userInfoList);
    }

    /***
     * 获取经办人信息
     * @author : 🐒
     */
    /**
     * 查询个人信息
     *
     * @return : user表,client表中所有的数据
     * @author : 🐒
     */
    @GetMapping("/personal/{uid}")
    public Result personal(@PathVariable Integer uid) {
        log.info("查询个人信息，uid为：{}", uid);
        return Result.success(userService.getUsersInfo(uid));
    }

    /***
     * 修改密码
     * @author : 🐒
     */
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
