package com.example.tourism_rear.service;

import com.example.tourism_rear.pack.pagination.PageBean;
import com.example.tourism_rear.pack.utils.EditUser;
import com.example.tourism_rear.pack.utils.Login;
import com.example.tourism_rear.pack.utils.Register;
import com.example.tourism_rear.pack.utils.UserInfo;

import java.util.List;

public interface UserService {

    void addUserInfo(UserInfo userInfo);

    void registerUser(Register registerInfo);

    PageBean list(Integer page,
                  Integer pageSize,
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
                  Boolean exactMatch);

    void deleteUser(List<Integer> uid);

    void updateUserInfo(EditUser editUser);

    boolean multiIsUniqueUsers(String uid);

    UserInfo getChangeUsersInfo(String uid);

    Login login(Login login);

    boolean isUniqueUsername(String username);

    int getUidByUsername(String username);

    int getAuthority(String username);

    UserInfo getUsersInfo(Integer uid);

    boolean updatePassword(Integer uid, String oldPassword, String newPassword);

    String getEmailByUsername(String username);

    String getUsernameByEmail(String email);

    boolean resetPasswordByEmail(String email, String newPassword);
}
