package com.shirongbao.timenest.service.chat.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.dao.ChatSessionsMapper;
import com.shirongbao.timenest.pojo.entity.ChatSessions;
import com.shirongbao.timenest.service.chat.ChatSessionsService;
import org.springframework.stereotype.Service;

/**
 * @author: ShiRongbao
 * @date: 2025-07-18
 * @description: 聊天会话服务实现类
 */
@Service("chatSessionsService")
public class ChatSessionsServiceImpl extends ServiceImpl<ChatSessionsMapper, ChatSessions> implements ChatSessionsService{
}
