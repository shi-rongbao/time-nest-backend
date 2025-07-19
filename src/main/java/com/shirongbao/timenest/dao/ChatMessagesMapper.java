package com.shirongbao.timenest.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shirongbao.timenest.pojo.entity.ChatMessages;
import com.shirongbao.timenest.pojo.vo.ChatMessageVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-06-03
 * @description: 聊天消息持久层接口
 */
public interface ChatMessagesMapper extends BaseMapper<ChatMessages> {

    // 根据游标查询消息列表
    List<ChatMessageVo> selectMessageByCursor(@Param("sessionId") Long sessionId, @Param("cursor") Long cursor, @Param("limit") int limit);

}
