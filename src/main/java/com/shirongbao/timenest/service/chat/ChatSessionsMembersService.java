package com.shirongbao.timenest.service.chat;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.pojo.entity.ChatSessionsMembers;

/**
 * @author: ShiRongbao
 * @date: 2025-07-18
 * @description: 聊天会话成员服务接口
 */
public interface ChatSessionsMembersService extends IService<ChatSessionsMembers> {

    // 新增未读数
    void increUnreadCount(Long sessionId, Long senderId);
}
