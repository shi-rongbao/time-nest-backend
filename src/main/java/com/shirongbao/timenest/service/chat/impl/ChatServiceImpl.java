package com.shirongbao.timenest.service.chat.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.timenest.common.entity.PageResult;
import com.shirongbao.timenest.common.enums.ChatSessionsTypeEnum;
import com.shirongbao.timenest.dao.ChatSessionMembersMapper;
import com.shirongbao.timenest.pojo.bo.ChatSessionBo;
import com.shirongbao.timenest.pojo.bo.ChatSessionTargetUserBo;
import com.shirongbao.timenest.pojo.dto.ChatSessionDto;
import com.shirongbao.timenest.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author: ShiRongbao
 * @date: 2025-07-15
 * @description: 聊天服务实现类
 */
@Service("chatService")
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMembersMapper chatSessionMembersMapper;

    @Override
    public PageResult<ChatSessionBo> getSessions(Integer pageNum, Integer pageSize, ChatSessionDto chatSessionDto) {
        long currentUserId = StpUtil.getLoginIdAsLong();
        chatSessionDto.setUserId(currentUserId);

        Page<ChatSessionBo> page = new Page<>(pageNum, pageSize);

        // 2. 直接调用分页查询方法，插件会自动处理分页和COUNT
        IPage<ChatSessionBo> sessionPage = chatSessionMembersMapper.querySessions(page, chatSessionDto);
        List<ChatSessionBo> sessions = sessionPage.getRecords();

        // 4. 空数据直接返回
        if (CollectionUtils.isEmpty(sessions)) {
            return new PageResult<>();
        }

        // 5. 按会话类型分组处理
        Map<Integer, List<ChatSessionBo>> sessionsByType = sessions.stream()
                .collect(Collectors.groupingBy(ChatSessionBo::getSessionType));

        // 6. 批量处理单聊会话的目标用户信息
        List<ChatSessionBo> singleSessions = sessionsByType.get(ChatSessionsTypeEnum.SINGLE.getCode());
        if (!CollectionUtils.isEmpty(singleSessions)) {
            enrichSingleSessionsWithTargetUsers(singleSessions, currentUserId);
        }

        // 7. 统一设置显示字段
        sessions.forEach(this::setDisplayFields);

        return new PageResult<>(sessions, (int)sessionPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 批量enrichment单聊会话的目标用户信息
     */
    private void enrichSingleSessionsWithTargetUsers(List<ChatSessionBo> singleSessions, long currentUserId) {
        // 提取会话ID
        List<Long> sessionIds = singleSessions.stream()
                .map(ChatSessionBo::getSessionId)
                .collect(Collectors.toList());

        // 批量查询目标用户信息（包含用户详情）
        List<ChatSessionTargetUserBo> targetUsers = chatSessionMembersMapper.queryTargetUsersWithUserInfo(sessionIds, currentUserId);

        // 构建会话ID到目标用户信息的映射
        Map<Long, ChatSessionTargetUserBo> sessionUserMap = targetUsers.stream()
                .collect(Collectors.toMap(
                        ChatSessionTargetUserBo::getSessionId,
                        Function.identity(),
                        (existing, replacement) -> {
                            log.warn("发现重复的会话ID: {}", existing.getSessionId());
                            return existing;
                        }
                ));

        // 设置目标用户信息
        singleSessions.forEach(session -> {
            ChatSessionTargetUserBo targetUser = sessionUserMap.get(session.getSessionId());
            if (targetUser != null) {
                session.setTargetUserId(targetUser.getTargetUserId());
                session.setTargetNickName(targetUser.getTargetNickName());
                session.setTargetAvatarUrl(targetUser.getTargetAvatarUrl());
            } else {
                log.warn("未找到会话{}的目标用户信息", session.getSessionId());
            }
        });
    }

    /**
     * 统一设置显示字段
     */
    private void setDisplayFields(ChatSessionBo session) {
        if (session.getSessionType() == ChatSessionsTypeEnum.SINGLE.getCode()) {
            session.setDisplayName(StringUtils.isNotBlank(session.getTargetNickName()) ?
                    session.getTargetNickName() : "未知用户");
            session.setDisplayAvatar(StringUtils.isNotBlank(session.getTargetAvatarUrl()) ?
                    session.getTargetAvatarUrl() : "");
        } else {
            session.setDisplayName(StringUtils.isNotBlank(session.getGroupName()) ?
                    session.getGroupName() : "群聊");
            session.setDisplayAvatar(StringUtils.isNotBlank(session.getGroupAvatar()) ?
                    session.getGroupAvatar() : "");
        }
    }

}
