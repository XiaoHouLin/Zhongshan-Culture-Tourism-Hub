package com.example.tourism_rear.service.impl;

import com.example.tourism_rear.mapper.MessageMapper;
import com.example.tourism_rear.mapper.OrderMapper;
import com.example.tourism_rear.mapper.UserMapper;
import com.example.tourism_rear.pack.pagination.PageBean;
import com.example.tourism_rear.pack.utils.EditUser;
import com.example.tourism_rear.pack.utils.Login;
import com.example.tourism_rear.pack.utils.Register;
import com.example.tourism_rear.service.UserService;
import com.example.tourism_rear.pack.utils.UserInfo;
import com.example.tourism_rear.service.BalanceChangeService;
import com.example.tourism_rear.service.MessageService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class UserImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MessageService messageService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private BalanceChangeService balanceChangeService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 添加用户
     *
     * @author : 🐒
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addUserInfo(UserInfo userInfo) {
        if (userInfo != null) {
            userInfo.setCphone(normalizePhone(userInfo.getCphone()));
        }
        userInfo.setPassword(encodePasswordIfNeeded(userInfo.getPassword()));
        if (userInfo.getCdate() == null) {
            userInfo.setCdate(new Date());
        }
        if (userInfo.getBalance() == null) {
            userInfo.setBalance(0.0);
        }
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
        if (registerInfo != null) {
            registerInfo.setCphone(normalizePhone(registerInfo.getCphone()));
        }
        registerInfo.setPassword(encodePasswordIfNeeded(registerInfo.getPassword()));
        userMapper.registerUser(registerInfo);
        registerInfo.setUid(getUidByUsername(registerInfo.getUsername()));
        String uidStr = Integer.toString(registerInfo.getUid());
        String lastThreeDigits = uidStr.length() > 3 ? uidStr.substring(uidStr.length() - 3) : uidStr;
        registerInfo.setCno("C" + lastThreeDigits);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        registerInfo.setCdate(now.format(formatter));
        userMapper.registerClient(registerInfo);
        messageService.createMessageIfAbsent(
                registerInfo.getUid(),
                "REGISTER",
                "register",
                "注册成功",
                "欢迎加入中山文旅系统，开启你的城市文化探索之旅。"
        );
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
                         String caddress, String cphone, String startDate, String endDate, String cremark, Double balance,
                         Boolean exactMatch) {
        try (Page<?> ignored = PageHelper.startPage(page, pageSize)) {
            List<UserInfo> userInfoList = userMapper.list(uid, username,
                    email, authority, cno, cname, csex, cage,
                    caddress, cphone, startDate, endDate, cremark, balance, exactMatch);
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
        if (uid == null || uid.isEmpty()) {
            return;
        }
        for (Integer item : uid) {
            if (item == null) {
                continue;
            }
            orderMapper.deleteOrderDetailsByUid(item);
            orderMapper.deleteOrdersByUid(item);
            messageMapper.deleteByUid(item);
        }
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
        Double beforeBalance = null;
        if (editUser != null && editUser.getUid() != null) {
            UserInfo existing = userMapper.getUsersInfo(editUser.getUid());
            if (existing != null) {
                beforeBalance = existing.getBalance();
            }
        }
        if (editUser != null) {
            editUser.setCphone(normalizePhone(editUser.getCphone()));
        }
        userMapper.updateUser(editUser);
        userMapper.updateClient(editUser);
        if (editUser == null || editUser.getUid() == null) {
            return;
        }
        Double afterBalance = userMapper.getBalance(editUser.getUid());
        double oldValue = beforeBalance == null ? 0D : beforeBalance;
        double newValue = afterBalance == null ? 0D : afterBalance;
        double delta = newValue - oldValue;
        if (Math.abs(delta) < 0.000001D) {
            return;
        }
        String reason = delta > 0
                ? "账户充值（管理员调整）"
                : "账户余额调整（管理员扣减）";
        balanceChangeService.recordChange(
                editUser.getUid(),
                delta > 0 ? "RECHARGE" : "ADMIN_ADJUST",
                reason,
                delta,
                oldValue,
                newValue,
                1
        );
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
        String storedPassword = userMapper.getPasswordByUid(uid);
        if (!passwordMatches(oldPassword, storedPassword)) {
            return false;
        }
        String encoded = encodePasswordIfNeeded(newPassword);
        return userMapper.updatePasswordByUid(uid, encoded) > 0;
    }

    @Override
    public String getEmailByUsername(String username) {
        return userMapper.getEmailByUsername(username);
    }

    @Override
    public String getUsernameByEmail(String email) {
        return userMapper.getUsernameByEmail(email);
    }

    @Override
    public boolean resetPasswordByEmail(String email, String newPassword) {
        String encoded = encodePasswordIfNeeded(newPassword);
        return userMapper.updatePasswordByEmail(email, encoded) > 0;
    }

    @Override
    public List<Integer> listAdminUids() {
        return userMapper.listAdminUids();
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
        if (login == null || !StringUtils.hasText(login.getUsername())) {
            return null;
        }
        Login stored = userMapper.getUserByUsername(login.getUsername());
        if (stored == null) {
            return null;
        }
        if (!passwordMatches(login.getPassword(), stored.getPassword())) {
            return null;
        }
        // 自动升级旧的明文密码为加密存储
        if (!isBcryptHash(stored.getPassword())) {
            String encoded = encodePasswordIfNeeded(login.getPassword());
            int uid = userMapper.getUidByUsername(login.getUsername());
            userMapper.updatePasswordByUid(uid, encoded);
        }
        return stored;
    }

    private String encodePasswordIfNeeded(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            return rawPassword;
        }
        if (isBcryptHash(rawPassword)) {
            return rawPassword;
        }
        return passwordEncoder.encode(rawPassword);
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    private String normalizePhone(String cphone) {
        if (!StringUtils.hasText(cphone)) {
            return cphone;
        }
        String digits = cphone.replaceAll("\\D", "");
        if (digits.startsWith("86") && digits.length() > 11) {
            digits = digits.substring(2);
        }
        return digits;
    }

    private boolean isBcryptHash(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}

