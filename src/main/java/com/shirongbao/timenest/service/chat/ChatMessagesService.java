package com.shirongbao.timenest.service.chat;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.pojo.entity.ChatMessages;
import com.shirongbao.timenest.pojo.vo.ChatMessageVo;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-06-03
 * @description: 聊天消息服务接口
 */
public interface ChatMessagesService extends IService<ChatMessages> {

    // 根据游标查询消息列表
    List<ChatMessageVo> selectMessageByCursor(Long sessionId, Long cursor, int limit);

}