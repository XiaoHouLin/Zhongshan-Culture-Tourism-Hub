package com.example.tourism_rear.service;

import com.example.tourism_rear.pack.pagination.PageBean;
import com.example.tourism_rear.pack.utils.EditUser;
import com.example.tourism_rear.pack.utils.Login;
import com.example.tourism_rear.pack.utils.Register;
import com.example.tourism_rear.pack.utils.UserInfo;

import java.util.List;

public interface UserService {
    /**
     * 新增用户
     *
     * @author : 🐒
     */
    void addUserInfo(UserInfo userInfo);

    /**
     * 注册用户
     *
     * @author : 🐒
     */
    void registerUser(Register registerInfo);

    /**
     * 查询单页用户所有信息
     *
     * @author : 🐒
     */
    PageBean list(Integer page, Integer pageSize, Integer uid, String username,
                  String email, Integer authority, String cno, String cname, String csex, Integer cage,
                  String caddress, String cphone, String csymptom, String startDate, String endDate, String cremark, Double balance);

    /**
     * 删除用户(不可撤销)
     *
     * @author : 🐒
     */
    void deleteUser(List<Integer> uid);

    /**
     * 修改用户信息
     *
     * @author : 🐒
     */
    void updateUserInfo(EditUser editUser);

    /**
     * 检测用户主键是否重复
     *
     * @author : 🐒
     */
//    boolean isUniqueUsers(String changeUid, String uid);
    boolean multiIsUniqueUsers(String uid);

    /**
     * 数据回显
     */
    UserInfo getChangeUsersInfo(String uid);

    /**
     * 用户登录
     */
    Login login(Login login);

    /**
     * 检测用户名是否重复
     */
    boolean isUniqueUsername(String username);

    /**
     * 通过用户名获取uid
     */
    int getUidByUsername(String username);

    /**
     * 通过用户名获取权限
     */
    int getAuthority(String username);

    /**
     * 通过uid获取用户信息
     */
    UserInfo getUsersInfo(Integer uid);

    /**
     * 修改密码
     */
    boolean updatePassword(Integer uid, String oldPassword, String newPassword);

    /**
     * 获取经办人信息（编号，名字，年龄，性别）
     */
    List<UserInfo> getAgency();
}
