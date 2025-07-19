package com.shirongbao.timenest.service.chat;

import com.shirongbao.timenest.common.entity.PageResult;
import com.shirongbao.timenest.pojo.bo.ChatSessionBo;
import com.shirongbao.timenest.pojo.dto.ChatSessionDto;
import com.shirongbao.timenest.pojo.vo.MessageHistoryVo;

/**
 * @author: ShiRongbao
 * @date: 2025-07-15
 * @description: 聊天服务接口
 */
public interface ChatService {

    // 分页获取会话列表
    PageResult<ChatSessionBo> getSessions(Integer pageNum, Integer pageSize, ChatSessionDto chatSessionDto);

    // 查询单聊会话信息
    ChatSessionBo findSingleSession(Long targetId);

    // 获取历史消息
    MessageHistoryVo getHistoryMessage(Long sessionId, Long cursor, Integer pageSize);
}
