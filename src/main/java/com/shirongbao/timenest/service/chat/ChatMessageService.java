package com.shirongbao.timenest.service.chat;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.pojo.entity.ChatMessage;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-06-03
 * @description: 聊天消息服务接口
 */
public interface ChatMessageService extends IService<ChatMessage> {

    // 获取当前用户所有的未读消息
    List<ChatMessage> getUnreadMessages(Long userId);

    // 保存消息
    boolean saveChatMessage(ChatMessage chatMessage);

    // 标记消息为已读！
    void markMessagesAsRead(Long readerId);
}