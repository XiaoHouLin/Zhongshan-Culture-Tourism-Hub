package com.example.tourism_rear.mapper;

import com.example.tourism_rear.pack.utils.WithdrawRequestRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WithdrawRequestMapper {

    @Insert("INSERT INTO withdraw_request(uid, amount, phone, status, create_time, update_time) " +
            "VALUES(#{uid}, #{amount}, #{phone}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(WithdrawRequestRecord record);

    @Select("SELECT COUNT(1) FROM withdraw_request WHERE uid = #{uid} AND DATE(create_time) = CURDATE()")
    int countTodayByUid(@Param("uid") Integer uid);

    @Select("SELECT wr.id, wr.uid, u.username AS username, c.cname AS cname, wr.amount, wr.phone, wr.status, " +
            "wr.reject_reason AS rejectReason, wr.review_uid AS reviewUid, wr.create_time AS createTime, wr.update_time AS updateTime " +
            "FROM withdraw_request wr " +
            "LEFT JOIN user u ON u.uid = wr.uid " +
            "LEFT JOIN client c ON c.uid = wr.uid " +
            "WHERE wr.id = #{id} LIMIT 1")
    WithdrawRequestRecord findById(@Param("id") Long id);

    @Select("SELECT wr.id, wr.uid, u.username AS username, c.cname AS cname, wr.amount, wr.phone, wr.status, " +
            "wr.reject_reason AS rejectReason, wr.review_uid AS reviewUid, wr.create_time AS createTime, wr.update_time AS updateTime " +
            "FROM withdraw_request wr " +
            "LEFT JOIN user u ON u.uid = wr.uid " +
            "LEFT JOIN client c ON c.uid = wr.uid " +
            "WHERE wr.uid = #{uid} AND wr.status = 0 ORDER BY wr.create_time DESC LIMIT 1")
    WithdrawRequestRecord findLatestPendingByUid(@Param("uid") Integer uid);

    @Select("SELECT wr.id, wr.uid, u.username AS username, c.cname AS cname, wr.amount, wr.phone, wr.status, " +
            "wr.reject_reason AS rejectReason, wr.review_uid AS reviewUid, wr.create_time AS createTime, wr.update_time AS updateTime " +
            "FROM withdraw_request wr " +
            "LEFT JOIN user u ON u.uid = wr.uid " +
            "LEFT JOIN client c ON c.uid = wr.uid " +
            "ORDER BY wr.create_time DESC, wr.id DESC")
    List<WithdrawRequestRecord> listAll();

    @Update("UPDATE withdraw_request SET status = 1, review_uid = #{reviewUid}, reject_reason = NULL, " +
            "update_time = #{updateTime} WHERE id = #{id} AND status = 0")
    int approve(@Param("id") Long id,
                @Param("reviewUid") Integer reviewUid,
                @Param("updateTime") java.time.LocalDateTime updateTime);

    @Update("UPDATE withdraw_request SET status = 2, review_uid = #{reviewUid}, reject_reason = #{rejectReason}, " +
            "update_time = #{updateTime} WHERE id = #{id} AND status = 0")
    int reject(@Param("id") Long id,
               @Param("reviewUid") Integer reviewUid,
               @Param("rejectReason") String rejectReason,
               @Param("updateTime") java.time.LocalDateTime updateTime);
}
