package com.shirongbao.timenest.service.chat;

import java.util.Set;
import java.util.List;
import java.util.Map;

/**
 * @author: ShiRongbao
 * @date: 2025-05-29
 * @description: 用户状态服务接口  这些方法现在专用于维护和查询 WebSocket 连接的在线状态
 */
public interface UserPresenceService {

    /**
     * 将用户标记为 WebSocket 在线（在 WebSocket 连接建立时调用）
     *
     * @param userId 用户ID
     */
    void userOnline(Long userId);

    /**
     * 将用户标记为 WebSocket 离线（在 WebSocket 连接关闭时调用）
     *
     * @param userId 用户ID
     */
    void userOffline(Long userId);

    /**
     * 更新用户 WebSocket 心跳（在收到 WebSocket 消息或心跳帧时调用）
     *
     * @param userId 用户ID
     */
    void updateHeartbeat(Long userId);

    /**
     * 检查用户是否 WebSocket 在线
     *
     * @param userId 用户ID
     * @return 是否 WebSocket 在线
     */
    boolean isUserOnline(Long userId);

    /**
     * 获取所有 WebSocket 在线用户ID
     *
     * @return WebSocket 在线用户ID列表
     */
    Set<String> getAllOnlineUsers();

    /**
     * 批量检查用户 WebSocket 在线状态
     *
     * @param userIds 用户ID列表
     * @return 用户在线状态映射
     */
    Map<Long, Boolean> batchCheckOnlineStatus(List<Long> userIds);

}
