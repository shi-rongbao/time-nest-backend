package com.shirongbao.timenest.service.chat.impl;

import com.shirongbao.timenest.common.constant.RedisConstant;
import com.shirongbao.timenest.service.chat.UserPresenceService;
import com.shirongbao.timenest.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
 * @description: 用户在线服务实现类
 */
@Service("userPresenceService")
@RequiredArgsConstructor
@Slf4j
public class UserPresenceServiceImpl implements UserPresenceService {

    private final RedisUtil redisUtil;

    /**
     * 用户上线注册到session registration
     * @param userId 用户ID
     */
    public void userOnline(Long userId) {
        try {
            // 1. 添加到在线用户集合
            redisUtil.sSet(RedisConstant.ONLINE_USERS_SET, userId.toString());

            // 2. 存储用户会话详细信息 (Hash结构)
            Map<String, String> sessionInfo = new HashMap<>();
            String currentTimeMillisString = Long.toString(System.currentTimeMillis());
            sessionInfo.put("onlineTime", currentTimeMillisString);
            sessionInfo.put("lastHeartbeat", currentTimeMillisString);
            sessionInfo.put("status", "online");
            redisUtil.add(RedisConstant.USER_SESSION_PREFIX + userId, sessionInfo);

            // 用户会话信息通常也需要过期时间，这里与上线时心跳的过期逻辑保持一致，例如2小时，或根据业务调整
            // 如果会话信息不过期，或者由心跳/离线逻辑管理，可以不设expire，或设置较长过期时间
            // 此处假设会话信息也设置24小时过期，与原代码一致
            redisUtil.expire(RedisConstant.USER_SESSION_PREFIX + userId, Duration.ofHours(24).toMillis());


            // 3. 初始化心跳时间，设置过期时间 (例如2分钟，与原代码一致)
            redisUtil.set(RedisConstant.USER_HEARTBEAT_PREFIX + userId, currentTimeMillisString);
            redisUtil.expire(RedisConstant.USER_HEARTBEAT_PREFIX + userId, Duration.ofMinutes(2).toMillis());

            log.info("用户上线: userId={}", userId);
        } catch (Exception e) {
            log.error("用户上线状态更新失败: userId={}", userId, e);
        }
    }

    /**
     * 用户离线
     * @param userId 用户ID
     */
    public void userOffline(Long userId) {
        try {
            // 1. 从在线用户集合中移除
            redisUtil.remove(RedisConstant.ONLINE_USERS_SET, userId.toString());

            // 2. 删除用户会话详细信息
            redisUtil.del(RedisConstant.USER_SESSION_PREFIX + userId);

            // 3. 删除用户心跳记录
            redisUtil.del(RedisConstant.USER_HEARTBEAT_PREFIX + userId);

            log.info("用户离线: userId={}", userId);
        } catch (Exception e) {
            log.error("用户离线状态更新失败: userId={}", userId, e);
        }
    }

    @Override
    public boolean isFriend(Long senderId, Long receiverId) {
        return false;
    }

    /**
     * 更新用户心跳
     * @param userId 用户ID
     */
    public void updateHeartbeat(Long userId) {
        try {
            String currentTimeMillisString = Long.toString(System.currentTimeMillis());

            // 1. 更新心跳时间，重置过期时间
            redisUtil.set(RedisConstant.USER_HEARTBEAT_PREFIX + userId, currentTimeMillisString);
            redisUtil.expire(RedisConstant.USER_HEARTBEAT_PREFIX + userId, Duration.ofMinutes(2).toMillis());

            // 2. 更新会话信息中的心跳时间
            // 首先获取当前会话信息
            Map<Object, Object> sessionInfo = redisUtil.getHashEntries(RedisConstant.USER_SESSION_PREFIX + userId);
            // 可能用户已离线或会话已过期
            if (sessionInfo == null) {
                // 直接抛出异常
                throw new RuntimeException("用户已离线或会话已过期");
            }
            sessionInfo.put("lastHeartbeat", currentTimeMillisString);
            // 转成Map<String, String>
            Map<String, String> sessionInfoMap = new HashMap<>();
            for (Map.Entry<Object, Object> entry : sessionInfo.entrySet()) {
                sessionInfoMap.put(entry.getKey().toString(), entry.getValue().toString());
            }
            // 刷新心跳时间
            redisUtil.add(RedisConstant.USER_SESSION_PREFIX + userId, sessionInfoMap);
            redisUtil.expire(RedisConstant.USER_SESSION_PREFIX + userId, Duration.ofMinutes(2).toMillis());

            log.info("心跳更新成功: userId={}", userId);
        } catch (Exception e) {
            log.error("心跳更新失败: userId={}", userId, e);
        }
    }

    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        try {
            // 1. 检查是否在在线用户集合中 (这是一个快速判断，但不完全依赖它)
            if (redisUtil.isMember(RedisConstant.ONLINE_USERS_SET, userId)) {
                log.debug("用户 {} 不在在线集合中", userId);
                return false;
            }

            // 2. 检查心跳时间 (更可靠的判断)
            String heartbeatString = (String) redisUtil.get(RedisConstant.USER_HEARTBEAT_PREFIX + userId);
            if (heartbeatString == null) {
                log.debug("用户 {} 心跳键不存在", userId);
                // 如果心跳不存在，但仍在在线集合中，说明状态可能不一致，执行离线逻辑清理
                userOffline(userId);
                return false;
            }

            long lastHeartbeat = Long.parseLong(heartbeatString);
            long currentTime = System.currentTimeMillis();

            // 心跳超时，认为用户已离线
            long heartbeatTimeoutMillis = Duration.ofMinutes(3).toMillis();
            if ((currentTime - lastHeartbeat) > heartbeatTimeoutMillis) {
                log.info("用户 {} 心跳超时 ({} ms)", userId, currentTime - lastHeartbeat);
                // 心跳超时，标记为离线
                userOffline(userId);
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            log.error("心跳时间格式错误: userId={}, value={}", userId, redisUtil.get(RedisConstant.USER_HEARTBEAT_PREFIX + userId), e);
            // 数据异常，也按离线处理
            userOffline(userId);
            return false;
        } catch (Exception e) {
            log.error("检查用户在线状态失败: userId={}", userId, e);
            // 发生其他异常，保守返回false
            return false;
        }
    }

    /**
     * 获取所有在线用户ID
     * @return 在线用户ID列表 (Set<String>)
     */
    public Set<String> getAllOnlineUsers() {
        try {
            // 假设 redisUtil.sMembers(key) 返回 Set<String>
            Set<Object> onlineUsers = redisUtil.members(RedisConstant.ONLINE_USERS_SET);
            if (CollectionUtils.isEmpty(onlineUsers)) {
                return new HashSet<>();
            }
            return onlineUsers.stream().map(Object::toString).collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("获取所有在线用户失败", e);
            // 返回空集合而不是null
            return new HashSet<>();
        }
    }

    /**
     * 批量检查用户在线状态
     * @param userIds 用户ID列表
     * @return 用户在线状态映射
     */
    public Map<Long, Boolean> batchCheckOnlineStatus(List<Long> userIds) {
        Map<Long, Boolean> result = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        for (Long userId : userIds) {
            // 添加null检查
            if (userId != null) {
                result.put(userId, isUserOnline(userId));
            }
        }
        return result;
    }

    /**
     * 清理过期的在线状态（定时任务调用）
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void cleanExpiredOnlineStatus() {
        log.debug("开始执行清理过期在线状态任务...");
        try {
            // 这已经是 Set<String>
            Set<String> onlineUserIds = getAllOnlineUsers();
            if (onlineUserIds.isEmpty()) {
                log.debug("没有在线用户需要清理。");
                return;
            }

            long currentTime = System.currentTimeMillis();
            // 心跳超时定义，3分钟
            long heartbeatExpiryDurationMillis = Duration.ofMinutes(3).toMillis();


            for (String userIdStr : onlineUserIds) {
                Long userId = null;
                try {
                    userId = Long.valueOf(userIdStr);
                } catch (NumberFormatException e) {
                    log.error("清理任务：无效的用户ID格式: {}", userIdStr, e);
                    continue;
                }

                String heartbeatString = (String) redisUtil.get(RedisConstant.USER_HEARTBEAT_PREFIX + userId);

                if (heartbeatString == null) {
                    log.info("清理过期在线状态: userId={} 的心跳键不存在，标记为离线。", userId);
                    // userOffline会从ONLINE_USERS_SET移除
                    userOffline(userId);
                    continue;
                }

                try {
                    long lastHeartbeat = Long.parseLong(heartbeatString);
                    // 如果 (当前时间 - 最后心跳时间) > 心跳过期阈值 (例如5分钟)
                    if ((currentTime - lastHeartbeat) > heartbeatExpiryDurationMillis) {
                        log.info("清理过期在线状态: userId={} 心跳超时 ({} ms)，标记为离线。", userId, (currentTime - lastHeartbeat));
                        userOffline(userId);
                    }
                } catch (NumberFormatException e) {
                    log.error("清理任务：userId={} 的心跳时间格式错误，value={}。标记为离线。", userId, heartbeatString, e);
                    // 数据异常，按离线处理
                    userOffline(userId);
                }
            }
            log.debug("清理过期在线状态任务完成。");
        } catch (Exception e) {
            // 捕获更广泛的异常，以防定时任务因未捕获的错误而停止
            log.error("清理过期在线状态失败", e);
        }
    }

}