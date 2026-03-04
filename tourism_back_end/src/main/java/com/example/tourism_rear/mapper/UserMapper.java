package com.example.tourism_rear.mapper;

import com.example.tourism_rear.pack.utils.EditUser;
import com.example.tourism_rear.pack.utils.Login;
import com.example.tourism_rear.pack.utils.Register;
import com.example.tourism_rear.pack.utils.UserInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {

    List<UserInfo> list(Integer uid,
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

    void insertUser(UserInfo userInfo);

    @Insert("INSERT INTO user(username, password, email, authority) VALUES (#{username},#{password},#{email},#{authority})")
    void registerUser(Register registerInfo);

    @Insert("INSERT INTO client(cno, cname, csex, cage, caddress, cphone, cdate, uid, balance) VALUES (#{cno},#{cname},#{csex},#{cage},#{caddress},#{cphone},#{cdate},#{uid},#{balance})")
    void registerClient(Register registerInfo);

    @Select("select uid from user where username=#{username}")
    int getUidByUsername(String username);

    @Select("select email from user where binary username=#{username}")
    String getEmailByUsername(String username);

    @Select("select username from user where email=#{email}")
    String getUsernameByEmail(String email);

    void insertClient(UserInfo userInfo);

    void deleteUser(List<Integer> uid);

    void deleteClient(List<Integer> uid);

    void updateUser(EditUser editUser);

    void updateClient(EditUser editUser);

    @Update("update client set balance=#{newBalance} where uid=#{uid}")
    void updateBalance(Integer uid, Double newBalance);

    @Select("select count(*) from user where uid=#{uid}")
    int multiIsUniqueUsers(String uid);

    @Select("select count(*) from user where username=#{username}")
    int isUniqueUsername(String username);

    UserInfo getChangeUsersInfo(String uid);

    @Select("select * from user where binary username=#{username} and password=#{password}")
    Login getUserNameAndPassword(Login login);

    @Select("select username, password from user where binary username=#{username}")
    Login getUserByUsername(String username);

    @Select("select authority from user where username=#{username}")
    int getAuthorityByUsername(String username);

    @Select("select * from user,client where user.uid=client.uid and user.uid=#{uid}")
    UserInfo getUsersInfo(Integer uid);

    @Update("update user set password=#{newPassword} where uid=#{uid} and password=#{oldPassword}")
    int updatePassword(Integer uid, String oldPassword, String newPassword);

    @Update("update user set password=#{newPassword} where email=#{email}")
    int updatePasswordByEmail(String email, String newPassword);

    @Select("select password from user where uid=#{uid}")
    String getPasswordByUid(Integer uid);

    @Update("update user set password=#{newPassword} where uid=#{uid}")
    int updatePasswordByUid(Integer uid, String newPassword);

    @Select("select balance from client where uid=#{uid}")
    Double getBalance(Integer uid);

    @Select("select uid from user where authority = 1")
    List<Integer> listAdminUids();
}
