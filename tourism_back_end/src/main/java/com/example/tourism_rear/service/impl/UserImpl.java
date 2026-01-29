package com.example.tourism_rear.service.impl;

import com.example.tourism_rear.mapper.UserMapper;
import com.example.tourism_rear.pack.pagination.PageBean;
import com.example.tourism_rear.pack.utils.EditUser;
import com.example.tourism_rear.pack.utils.Login;
import com.example.tourism_rear.pack.utils.Register;
import com.example.tourism_rear.service.UserService;
import com.example.tourism_rear.pack.utils.UserInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 添加用户
     *
     * @author : 🐒
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addUserInfo(UserInfo userInfo) {
        userMapper.insertUser(userInfo);
        userInfo.setUid(userMapper.getUidByUsername(userInfo.getUsername()));
        String uidStr = Integer.toString(userInfo.getUid());
        String lastThreeDigits = uidStr.length() > 3 ? uidStr.substring(uidStr.length() - 3) : uidStr;
        userInfo.setCno("C" + lastThreeDigits);
        userMapper.insertClient(userInfo);
    }

    /**
     * 注册用户
     *
     * @author : 🐒
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void registerUser(Register registerInfo) {
        userMapper.registerUser(registerInfo);
        registerInfo.setUid(getUidByUsername(registerInfo.getUsername()));
        String uidStr = Integer.toString(registerInfo.getUid());
        String lastThreeDigits = uidStr.length() > 3 ? uidStr.substring(uidStr.length() - 3) : uidStr;
        registerInfo.setCno("C" + lastThreeDigits);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        registerInfo.setCdate(now.format(formatter));
        userMapper.registerClient(registerInfo);
    }

    /**
     * 查询用户所有信息
     *
     * @return : user表中所有的数据
     * @author : 🐒
     */
    @Override
    public PageBean list(Integer page, Integer pageSize, Integer uid, String username,
                         String email, Integer authority, String cno, String cname, String csex, Integer cage,
                         String caddress, String cphone, String csymptom, String startDate, String endDate, String cremark, Double balance) {
        try (Page<?> ignored = PageHelper.startPage(page, pageSize)) {
            List<UserInfo> userInfoList = userMapper.list(uid, username,
                    email, authority, cno, cname, csex, cage,
                    caddress, cphone, csymptom, startDate, endDate, cremark, balance);
            userInfoList.forEach(userInfo -> userInfo.setPassword("Unknown"));
            Page<UserInfo> userInfoPage = (Page<UserInfo>) userInfoList;
            return new PageBean(userInfoPage.getTotal(), userInfoPage.getResult());
        }
    }

    /**
     * 删除用户(不可撤销)
     *
     * @author : 🐒
     */
    @Transactional(rollbackFor = Exception.class)//事务
    @Override
    public void deleteUser(List<Integer> uid) {
        userMapper.deleteClient(uid);
        userMapper.deleteUser(uid);
    }

    /**
     * 更新用户信息
     *
     * @author : 🐒
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUserInfo(EditUser editUser) {
        userMapper.updateUser(editUser);
        userMapper.updateClient(editUser);
    }

    @Override
    public boolean multiIsUniqueUsers(String uid) {
        return userMapper.multiIsUniqueUsers(uid) <= 0;
    }

    /**
     * 检测用户名是否重复
     */
    @Override
    public boolean isUniqueUsername(String username) {
        return userMapper.isUniqueUsername(username) <= 0;
    }

    /**
     * 根据用户名获取用户主键
     */
    @Override
    public int getUidByUsername(String username) {
        return userMapper.getUidByUsername(username);
    }

    /**
     * 获取用户权限
     */
    @Override
    public int getAuthority(String username) {
        return userMapper.getAuthorityByUsername(username);
    }

    /**
     * 获取个人用户信息
     */
    @Override
    public UserInfo getUsersInfo(Integer uid) {
        UserInfo userInfo = userMapper.getUsersInfo(uid);
        userInfo.setPassword("Unknown");
        return userInfo;
    }

    /**
     * 根据uid修改密码
     */
    @Override
    public boolean updatePassword(Integer uid, String oldPassword, String newPassword) {
        return userMapper.updatePassword(uid, oldPassword, newPassword) > 0;
    }


    /**
     * 数据回显
     */
    @Override
    public UserInfo getChangeUsersInfo(String uid) {
        UserInfo userInfo = userMapper.getChangeUsersInfo(uid);
        userInfo.setPassword("Unknown");
        return userInfo;
    }

    /**
     * 用户登录
     */
    @Override
    public Login login(Login login) {
        return userMapper.getUserNameAndPassword(login);
    }
}

