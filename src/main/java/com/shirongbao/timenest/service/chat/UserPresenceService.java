package com.shirongbao.timenest.service.chat;

import java.util.Set;

/**
 * @author: ShiRongbao
 * @date: 2025-05-29
 * @description: 用户在线服务接口
 */
public interface UserPresenceService {

    void userOnline(Long userId);

    void updateHeartbeat(Long senderId);

    void userOffline(Long userId);

    boolean isFriend(Long senderId, Long receiverId);

    boolean isUserOnline(Long receiverId);

    Set<String> getAllOnlineUsers();
}
