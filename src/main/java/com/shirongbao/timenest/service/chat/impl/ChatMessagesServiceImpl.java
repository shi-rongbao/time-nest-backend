package com.shirongbao.timenest.service.chat.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.dao.ChatMessagesMapper;
import com.shirongbao.timenest.pojo.entity.ChatMessages;
import com.shirongbao.timenest.pojo.vo.ChatMessageVo;
import com.shirongbao.timenest.service.chat.ChatMessagesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author: ShiRongbao
 * @date: 2025-06-03
 * @description: 聊天消息服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessagesServiceImpl extends ServiceImpl<ChatMessagesMapper, ChatMessages> implements ChatMessagesService {

    private final ChatMessagesMapper chatMessagesMapper;

    @Override
    public List<ChatMessageVo> selectMessageByCursor(Long sessionId, Long cursor, int limit) {
        return chatMessagesMapper.selectMessageByCursor(sessionId, cursor, limit);
    }

}