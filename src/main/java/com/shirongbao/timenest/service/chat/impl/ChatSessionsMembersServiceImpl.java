package com.shirongbao.timenest.service.chat.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.dao.ChatSessionsMembersMapper;
import com.shirongbao.timenest.pojo.entity.ChatSessionsMembers;
import com.shirongbao.timenest.service.chat.ChatSessionsMembersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author: ShiRongbao
 * @date: 2025-07-18
 * @description: 聊天会话成员服务实现类
 */
@Service("chatSessionsMembersService")
@RequiredArgsConstructor
public class ChatSessionsMembersServiceImpl extends ServiceImpl<ChatSessionsMembersMapper, ChatSessionsMembers> implements ChatSessionsMembersService {

    private final ChatSessionsMembersMapper chatSessionsMembersMapper;

    @Override
    public void increUnreadCount(Long sessionId, Long senderId) {
        chatSessionsMembersMapper.increUnreadCount(sessionId, senderId);
    }
}
