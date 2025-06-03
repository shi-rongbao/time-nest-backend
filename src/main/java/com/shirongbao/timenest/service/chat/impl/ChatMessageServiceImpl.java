package com.shirongbao.timenest.service.chat.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.common.enums.ReadStatusEnum;
import com.shirongbao.timenest.dao.ChatMessageDao;
import com.shirongbao.timenest.pojo.entity.ChatMessage;
import com.shirongbao.timenest.service.chat.ChatMessageService;
import com.shirongbao.timenest.service.friend.FriendshipsService;
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
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageDao, ChatMessage> implements ChatMessageService {

    private final FriendshipsService friendshipsService;

    @Override
    public List<ChatMessage> getUnreadMessages(Long userId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getReceiverId, userId);
        wrapper.eq(ChatMessage::getReadStatus, ReadStatusEnum.UNREAD.getCode());
        wrapper.orderByAsc(ChatMessage::getCreatedAt);
        return list(wrapper);
    }

    @Override
    public boolean saveChatMessage(ChatMessage chatMessage) {
        return save(chatMessage);
    }

    /**
     * 将指定接收者的所有未读消息标记为已读
     * @param receiverId 接收者用户ID
     * @return 更新的记录数
     */
    @Override
    public void markMessagesAsRead(Long receiverId) {
        if (receiverId == null) {
            log.warn("尝试标记已读消息时接收者ID为空。");
            return;
        }

        // 构建更新条件
        LambdaUpdateWrapper<ChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
        // 筛选出接收者为当前用户，且状态为未读的消息
        updateWrapper.eq(ChatMessage::getReceiverId, receiverId);
        updateWrapper.eq(ChatMessage::getReadStatus, ReadStatusEnum.UNREAD.getCode());

        // 设置更新的字段
        ChatMessage updateEntity = new ChatMessage();
        // 设置为已读状态
        updateEntity.setReadStatus(ReadStatusEnum.READ_DONE.getCode());

        try {
            boolean rowsAffected = update(updateEntity, updateWrapper);
            log.info("用户 {} 的 {} 条消息被标记为已读。", receiverId, rowsAffected);
        } catch (Exception e) {
            log.error("标记用户 {} 消息为已读失败: {}", receiverId, e.getMessage(), e);
            throw new RuntimeException("出现异常！");
        }
    }
}