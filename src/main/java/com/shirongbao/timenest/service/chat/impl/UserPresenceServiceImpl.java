package com.shirongbao.timenest.service.chat.impl;

import com.shirongbao.timenest.common.constant.RedisConstant;
import com.shirongbao.timenest.service.chat.UserPresenceService;
import com.shirongbao.timenest.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: ShiRongbao
 * @date: 2025-05-29
 * @description: 用户在线服务实现类 (现在只管理 WebSocket 在线状态)
 */
@Service("userPresenceService")
@RequiredArgsConstructor
@Slf4j
public class UserPresenceServiceImpl implements UserPresenceService {

    private final RedisUtil redisUtil;

    /**
     * 用户上线（WebSocket 连接建立时调用）
     * 将用户ID添加到 WebSocket 在线用户集合，并记录会话信息和心跳。
     *
     * @param userId 用户ID
     */
    @Override
    public void userOnline(Long userId) {
        try {
            // 1. 添加到 WebSocket 在线用户集合 (Set)
            redisUtil.sSet(RedisConstant.ONLINE_USERS_SET, userId.toString());

            // 2. 存储用户 WebSocket 会话详细信息 (Hash)
            Map<String, String> sessionInfo = new HashMap<>();
            String currentTimeMillisString = Long.toString(System.currentTimeMillis());
            sessionInfo.put("onlineTime", currentTimeMillisString);
            sessionInfo.put("lastHeartbeat", currentTimeMillisString);
            sessionInfo.put("status", "websocket_online");
            redisUtil.add(RedisConstant.USER_SESSION_PREFIX + userId, sessionInfo);

            // 设置会话信息过期时间，例如 24 小时
            redisUtil.expire(RedisConstant.USER_SESSION_PREFIX + userId, Duration.ofHours(24).toMillis());

            // 3. 初始化心跳时间，设置过期时间 (例如 2 分钟，确保比客户端心跳间隔长)
            redisUtil.set(RedisConstant.USER_HEARTBEAT_PREFIX + userId, currentTimeMillisString);
            redisUtil.expire(RedisConstant.USER_HEARTBEAT_PREFIX + userId, Duration.ofMinutes(2).toMillis());

            log.info("用户 WebSocket 在线: userId={}", userId);
        } catch (Exception e) {
            log.error("用户 WebSocket 在线状态更新失败: userId={}", userId, e);
        }
    }

    /**
     * 用户离线（WebSocket 连接关闭时调用）
     * 从 Redis 中移除用户的所有 WebSocket 相关在线状态。
     *
     * @param userId 用户ID
     */
    @Override
    public void userOffline(Long userId) {
        try {
            // 1. 从 WebSocket 在线用户集合中移除
            redisUtil.remove(RedisConstant.ONLINE_USERS_SET, userId.toString());

            // 2. 删除用户 WebSocket 会话详细信息
            redisUtil.del(RedisConstant.USER_SESSION_PREFIX + userId);

            // 3. 删除用户心跳记录
            redisUtil.del(RedisConstant.USER_HEARTBEAT_PREFIX + userId);

            log.info("用户 WebSocket 离线: userId={}", userId);
        } catch (Exception e) {
            log.error("用户 WebSocket 离线状态更新失败: userId={}", userId, e);
        }
    }

    /**
     * 更新用户心跳（在收到 WebSocket 消息或心跳帧时调用）
     * 刷新用户在 Redis 中的心跳时间和会话信息过期时间。
     *
     * @param userId 用户ID
     */
    @Override
    public void updateHeartbeat(Long userId) {
        try {
            String currentTimeMillisString = Long.toString(System.currentTimeMillis());

            // 1. 更新心跳时间，重置过期时间 (WebSocket 心跳键)
            redisUtil.set(RedisConstant.USER_HEARTBEAT_PREFIX + userId, currentTimeMillisString);
            redisUtil.expire(RedisConstant.USER_HEARTBEAT_PREFIX + userId, Duration.ofMinutes(2).toMillis());

            // 2. 更新 WebSocket 会话信息中的心跳时间，并刷新其过期时间
            Map<Object, Object> sessionInfo = redisUtil.getHashEntries(RedisConstant.USER_SESSION_PREFIX + userId);
            if (sessionInfo == null) {
                // 如果用户心跳更新，但其会话信息已不存在（可能过期或被清理），则记录警告
                log.warn("用户 {} 心跳更新时，其 WebSocket 会话信息不存在或已过期。", userId);
                // 此时可以考虑重新调用 userOnline 或等待定时任务处理，这里为了简化先跳过
                return;
            }
            sessionInfo.put("lastHeartbeat", currentTimeMillisString);
            // 转换为 Map<String, String> 以便使用 redisUtil.add
            Map<String, String> sessionInfoMap = new HashMap<>();
            for (Map.Entry<Object, Object> entry : sessionInfo.entrySet()) {
                sessionInfoMap.put(entry.getKey().toString(), entry.getValue().toString());
            }
            redisUtil.add(RedisConstant.USER_SESSION_PREFIX + userId, sessionInfoMap);
            redisUtil.expire(RedisConstant.USER_SESSION_PREFIX + userId, Duration.ofMinutes(2).toMillis()); // 与心跳过期时间同步

            log.debug("用户 WebSocket 心跳更新成功: userId={}", userId);
        } catch (Exception e) {
            log.error("用户 WebSocket 心跳更新失败: userId={}", userId, e);
        }
    }

    /**
     * 检查用户是否 WebSocket 在线
     * 通过检查 Redis 中心跳键和在线集合来判断。
     *
     * @param userId 用户ID
     * @return 是否 WebSocket 在线
     */
    @Override
    public boolean isUserOnline(Long userId) {
        try {
            // 优先检查 Redis 中心跳键的存在和过期状态
            String heartbeatString = (String) redisUtil.get(RedisConstant.USER_HEARTBEAT_PREFIX + userId);
            if (heartbeatString == null) {
                log.debug("用户 {} WebSocket 心跳键不存在，判定为 WebSocket 离线。", userId);
                userOffline(userId); // 清理可能残留的在线集合状态
                return false;
            }

            long lastHeartbeat = Long.parseLong(heartbeatString);
            long currentTime = System.currentTimeMillis();

            // 定义心跳超时时间，略大于 Redis key 的过期时间
            long heartbeatTimeoutMillis = Duration.ofMinutes(3).toMillis();
            if ((currentTime - lastHeartbeat) > heartbeatTimeoutMillis) {
                log.info("用户 {} WebSocket 心跳超时 ({} ms)，判定为 WebSocket 离线。", userId, currentTime - lastHeartbeat);
                userOffline(userId);
                return false;
            }

            // 再次检查是否在 WebSocket 在线用户集合中（一个额外的同步校验）
            if (!redisUtil.isMember(RedisConstant.ONLINE_USERS_SET, userId.toString())) {
                log.warn("用户 {} 有心跳键但不在 WebSocket 在线集合中，修正为 WebSocket 离线。", userId);
                userOffline(userId);
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            log.error("WebSocket 心跳时间格式错误: userId={}, value={}", userId, redisUtil.get(RedisConstant.USER_HEARTBEAT_PREFIX + userId), e);
            userOffline(userId);
            return false;
        } catch (Exception e) {
            log.error("检查用户 WebSocket 在线状态失败: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 获取所有 WebSocket 在线用户ID
     *
     * @return WebSocket 在线用户ID列表
     */
    @Override
    public Set<String> getAllOnlineUsers() {
        try {
            Set<Object> onlineUsers = redisUtil.members(RedisConstant.ONLINE_USERS_SET);
            if (CollectionUtils.isEmpty(onlineUsers)) {
                return new HashSet<>();
            }
            return onlineUsers.stream().map(Object::toString).collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("获取所有 WebSocket 在线用户失败", e);
            return new HashSet<>();
        }
    }

    /**
     * 批量检查用户 WebSocket 在线状态
     *
     * @param userIds 用户ID列表
     * @return 用户在线状态映射
     */
    @Override
    public Map<Long, Boolean> batchCheckOnlineStatus(List<Long> userIds) {
        Map<Long, Boolean> result = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        for (Long userId : userIds) {
            if (userId != null) {
                result.put(userId, isUserOnline(userId));
            }
        }
        return result;
    }


    // 清理过期的 WebSocket 在线状态（定时任务）
    // 暂时注释掉 @Scheduled，后续统一启用
    // -------------------------------------------------------------
    // @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void cleanExpiredOnlineStatus() {
        log.debug("开始执行清理过期 WebSocket 在线状态任务...");
        try {
            Set<String> onlineUserIds = getAllOnlineUsers();
            if (onlineUserIds.isEmpty()) {
                log.debug("没有 WebSocket 在线用户需要清理。");
                return;
            }

            long currentTime = System.currentTimeMillis();
            long heartbeatExpiryDurationMillis = Duration.ofMinutes(3).toMillis();

            for (String userIdStr : onlineUserIds) {
                Long userId = null;
                try {
                    userId = Long.valueOf(userIdStr);
                } catch (NumberFormatException e) {
                    log.error("清理任务：无效的用户ID格式: {}", userIdStr, e);
                    redisUtil.remove(RedisConstant.ONLINE_USERS_SET, userIdStr); // 移除无效ID
                    continue;
                }

                String heartbeatString = (String) redisUtil.get(RedisConstant.USER_HEARTBEAT_PREFIX + userId);

                if (heartbeatString == null) {
                    log.info("清理 WebSocket 在线状态: userId={} 的心跳键不存在，标记为 WebSocket 离线。", userId);
                    userOffline(userId);
                    continue;
                }

                try {
                    long lastHeartbeat = Long.parseLong(heartbeatString);
                    if ((currentTime - lastHeartbeat) > heartbeatExpiryDurationMillis) {
                        log.info("清理 WebSocket 在线状态: userId={} 心跳超时 ({} ms)，标记为 WebSocket 离线。", userId, (currentTime - lastHeartbeat));
                        userOffline(userId);
                    }
                } catch (NumberFormatException e) {
                    log.error("清理任务：userId={} 的心跳时间格式错误，value={}。标记为 WebSocket 离线。", userId, heartbeatString, e);
                    userOffline(userId);
                }
            }
            log.debug("清理过期 WebSocket 在线状态任务完成。");
        } catch (Exception e) {
            log.error("清理过期 WebSocket 在线状态失败", e);
        }
    }
}