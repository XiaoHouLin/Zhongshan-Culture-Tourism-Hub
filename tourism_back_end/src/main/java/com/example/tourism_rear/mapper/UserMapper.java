package com.example.tourism_rear.mapper;

import com.example.tourism_rear.pack.utils.EditUser;
import com.example.tourism_rear.pack.utils.Login;
import com.example.tourism_rear.pack.utils.Register;
import com.example.tourism_rear.pack.utils.UserInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 查询所有用户信息
     *
     * @return List<UserInfo>
     */
    List<UserInfo> list(Integer uid, String username,
                        String email, Integer authority, String cno, String cname, String csex, Integer cage,
                        String caddress, String cphone, String csymptom, String startDate, String endDate, String cremark, Double balance);

    /**
     * 插入用户数据
     */
    void insertUser(UserInfo userInfo);

    /**
     * 注册时，插入用户数据
     */
    @Insert("INSERT INTO user(username, password, email, authority)" +
            " VALUES (#{username},#{password},#{email},#{authority})")
    void registerUser(Register registerInfo);

    @Insert("INSERT INTO client(cno, cname, csex, cage, caddress, cphone, cdate, uid, balance)" +
            " VALUES (#{cno},#{cname},#{csex},${cage},#{caddress},#{cphone},#{cdate},#{uid},#{balance})")
    void registerClient(Register registerInfo);

    /**
     * 根据username查找uid
     */
    @Select("select uid from user where username=#{username}")
    int getUidByUsername(String username);

    void insertClient(UserInfo userInfo);

    /**
     * 根据uid删除用户
     * 删除操作不可撤销！！
     *
     * @param uid 用户uid
     */
    void deleteUser(List<Integer> uid);

    void deleteClient(List<Integer> uid);

    /**
     * 修改用户信息
     */
    void updateUser(EditUser editUser);

    void updateClient(EditUser editUser);

    /**
     * 根据uid查找用户余额
     */
    @Update("update client set balance=#{newBalance} where uid=#{uid}")
    void updateBalance(Integer uid, Double newBalance);

    @Select("select count(*) from user where uid=#{uid}")
    int multiIsUniqueUsers(String uid);

    /**
     * 判断用户名是否重复
     */
    @Select("select count(*) from user where username=#{username}")
    int isUniqueUsername(String username);

    /**
     * 数据回显
     */
    UserInfo getChangeUsersInfo(String uid);

    /**
     * 登录
     */
    @Select("select * from user where binary username=#{username} and password=#{password}")
    Login getUserNameAndPassword(Login login);

    /**
     * 根据username查找权限
     */
    @Select("select authority from user where username=#{username}")
    int getAuthorityByUsername(String username);

    /**
     * 根据uid查找个人用户信息
     */
    @Select("select * from user,client where user.uid=client.uid and user.uid=#{uid}")
    UserInfo getUsersInfo(Integer uid);

    /**
     * 根据uid修改密码，通过旧密码校验
     */
    @Update("update user set password=#{newPassword} where uid=#{uid} and password=#{oldPassword}")
    int updatePassword(Integer uid, String oldPassword, String newPassword);

    /**
     * 根据uid查找余额
     */
    @Select("select balance from client where uid=#{uid}")
    Double getBalance(Integer uid);

}
