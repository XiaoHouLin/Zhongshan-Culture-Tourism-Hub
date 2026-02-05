package com.example.tourism_rear.mapper;

import com.example.tourism_rear.pack.utils.MessageNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {
    List<MessageNotice> listByUid(@Param("uid") Integer uid);

    int insertMessage(MessageNotice notice);

    int markRead(@Param("uid") Integer uid, @Param("id") Long id);

    int deleteMessage(@Param("uid") Integer uid, @Param("id") Long id);

    int countByTypeAndRef(@Param("uid") Integer uid,
                          @Param("messageType") String messageType,
                          @Param("refId") String refId);
}
