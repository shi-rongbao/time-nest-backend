package com.shirongbao.timenest.service.chat;

import com.shirongbao.timenest.common.entity.PageResult;
import com.shirongbao.timenest.pojo.bo.ChatSessionBo;
import com.shirongbao.timenest.pojo.dto.ChatSessionDto;
import com.shirongbao.timenest.pojo.dto.mq.ChatMessageMqDto;
import com.shirongbao.timenest.pojo.entity.ChatMessages;
import com.shirongbao.timenest.pojo.vo.MessageHistoryVo;

import java.util.List;

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

    // 校验当前用户是否属于这个会话
    boolean isUserMemberOfSession(Long userId, Long sessionId);

    // 根据sessionId获取成员Id列表
    List<Long> getMemberIdsBySessionId(Long sessionId);

    // 保存聊天消息
    ChatMessages saveChatMessages(ChatMessageMqDto mqDto);

    // 更新聊天会话表的摘要
    void updateChatSessionAbstract(ChatMessages chatMessages);

    // 新增未读数
    void increUnreadCount(Long sessionId, Long senderId);

    // 新用户自动加入拾光纪群组
    void joinTimeNestGroup(Long userId);
}
