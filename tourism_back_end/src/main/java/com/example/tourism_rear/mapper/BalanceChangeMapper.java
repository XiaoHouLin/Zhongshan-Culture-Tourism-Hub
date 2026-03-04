package com.example.tourism_rear.mapper;

import com.example.tourism_rear.pack.utils.BalanceChangeRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BalanceChangeMapper {

    @Insert("INSERT INTO balance_change(uid, change_type, reason, change_amount, before_balance, after_balance, status, create_time) " +
            "VALUES(#{uid}, #{changeType}, #{reason}, #{changeAmount}, #{beforeBalance}, #{afterBalance}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(BalanceChangeRecord record);

    @Select("SELECT id, uid, change_type AS changeType, reason, change_amount AS changeAmount, before_balance AS beforeBalance, " +
            "after_balance AS afterBalance, status, create_time AS createTime " +
            "FROM balance_change WHERE uid = #{uid} AND status = 1 ORDER BY create_time DESC, id DESC")
    List<BalanceChangeRecord> listByUid(@Param("uid") Integer uid);

    @Select("SELECT COUNT(1) FROM balance_change WHERE uid = #{uid} AND change_type = 'WITHDRAW_REQUEST' AND status = 0")
    int countPendingWithdrawRequest(@Param("uid") Integer uid);
}
