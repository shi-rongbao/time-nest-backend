package com.shirongbao.timenest.service.chat.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.timenest.common.entity.PageResult;
import com.shirongbao.timenest.common.enums.ChatSessionRoleType;
import com.shirongbao.timenest.common.enums.ChatSessionsTypeEnum;
import com.shirongbao.timenest.common.enums.IsDeletedEnum;
import com.shirongbao.timenest.common.enums.RecalledStatusEnum;
import com.shirongbao.timenest.common.exception.BusinessException;
import com.shirongbao.timenest.dao.ChatSessionsMembersMapper;
import com.shirongbao.timenest.pojo.bo.ChatSessionBo;
import com.shirongbao.timenest.pojo.bo.ChatSessionTargetUserBo;
import com.shirongbao.timenest.pojo.dto.ChatSessionDto;
import com.shirongbao.timenest.pojo.dto.mq.ChatMessageMqDto;
import com.shirongbao.timenest.pojo.entity.ChatMessages;
import com.shirongbao.timenest.pojo.entity.ChatSessionsMembers;
import com.shirongbao.timenest.pojo.entity.ChatSessions;
import com.shirongbao.timenest.pojo.entity.Users;
import com.shirongbao.timenest.pojo.vo.ChatMessageVo;
import com.shirongbao.timenest.pojo.vo.MessageHistoryVo;
import com.shirongbao.timenest.service.auth.UserService;
import com.shirongbao.timenest.service.chat.ChatMessagesService;
import com.shirongbao.timenest.service.chat.ChatService;
import com.shirongbao.timenest.service.chat.ChatSessionsMembersService;
import com.shirongbao.timenest.service.chat.ChatSessionsService;
import com.shirongbao.timenest.service.friend.FriendshipsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    private final ChatSessionsMembersMapper chatSessionsMembersMapper;

    private final ChatSessionsMembersService chatSessionsMembersService;

    private final ChatSessionsService chatSessionsService;

    private final FriendshipsService friendshipsService;

    private final UserService userService;

    private final ChatMessagesService chatMessagesService;

    private final Snowflake snowflake;

    @Override
    public PageResult<ChatSessionBo> getSessions(Integer pageNum, Integer pageSize, ChatSessionDto chatSessionDto) {
        long currentUserId = StpUtil.getLoginIdAsLong();
        chatSessionDto.setUserId(currentUserId);

        Page<ChatSessionBo> page = new Page<>(pageNum, pageSize);

        // 2. 直接调用分页查询方法，插件会自动处理分页和COUNT
        IPage<ChatSessionBo> sessionPage = chatSessionsMembersMapper.querySessions(page, chatSessionDto);
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

        return new PageResult<>(sessions, (int) sessionPage.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public ChatSessionBo findSingleSession(Long targetId) {
        long currentUserId = StpUtil.getLoginIdAsLong();
        if (currentUserId == targetId) {
            throw new BusinessException("天呐，你怎么还想跟自己聊天");
        }

        // 校验是否是好友
        boolean isFriend = friendshipsService.checkIsFriend(targetId);
        if (!isFriend) {
            throw new BusinessException("你们当前还不是好友哦~暂时不能聊天");
        }

        // 查询是否有单聊会话
        ChatSessionBo chatSessionBo = chatSessionsMembersMapper.querySingleSessionWithUserId(currentUserId, targetId);
        if (!ObjectUtils.isEmpty(chatSessionBo)) {
            return chatSessionBo;
        }

        // 创建两个用户的会话并返回结果
        return createSession(targetId, currentUserId);
    }

    @Override
    public MessageHistoryVo getHistoryMessage(Long sessionId, Long cursor, Integer pageSize) {
        long currentUserId = StpUtil.getLoginIdAsLong();
        // 基础校验，看当前用户是否属于会话的成员
        boolean check = isUserMemberOfSession(currentUserId, sessionId);
        if (!check) {
            throw new BusinessException("当前查询参数异常！");
        }

        // 传入limit为pageSize + 1，用于做hasMore记录
        List<ChatMessageVo> messages = chatMessagesService.selectMessageByCursor(sessionId, cursor, pageSize + 1);
        boolean hasMore = messages.size() > pageSize;

        if (hasMore) {
            // 只有当记录数真的超过pageSize时，才移除最后一条
            messages.remove(messages.size() - 1);
        }

        // 处理撤回消息 有撤回消息的在内存中置空内容
        messages.forEach(msg -> {
            if (msg.getRecalled() != null && msg.getRecalled().equals(RecalledStatusEnum.RECALLED.getCode())) {
                msg.setContent("");
            }
        });

        // 设置游标并返回
        MessageHistoryVo messageHistoryVo = new MessageHistoryVo();
        messageHistoryVo.setRecords(messages);
        messageHistoryVo.setHasMore(hasMore);
        if (!messages.isEmpty()) {
            // nextCursor是当前返回列表中的最后一条（也就是最老的一条）消息的ID
            messageHistoryVo.setNextCursor(messages.get(messages.size() - 1).getMessageId());
        } else {
            messageHistoryVo.setNextCursor(null);
        }

        return messageHistoryVo;
    }

    // 用户是否属于会话
    public boolean isUserMemberOfSession(Long userId, Long sessionId) {
        LambdaQueryWrapper<ChatSessionsMembers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSessionsMembers::getUserId, userId);
        wrapper.eq(ChatSessionsMembers::getSessionId, sessionId);
        wrapper.eq(ChatSessionsMembers::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());

        long count = chatSessionsMembersService.count(wrapper);
        return count != 0;
    }

    @Override
    public List<Long> getMemberIdsBySessionId(Long sessionId) {
        LambdaQueryWrapper<ChatSessionsMembers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSessionsMembers::getSessionId, sessionId);
        wrapper.eq(ChatSessionsMembers::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());

        wrapper.select(ChatSessionsMembers::getUserId);
        // 只需要所有成员的id即可
        return chatSessionsMembersService.listObjs(wrapper, id -> (Long) id);
    }

    @Override
    public ChatMessages saveChatMessages(ChatMessageMqDto mqDto) {
        ChatMessages chatMessages = new ChatMessages();

        chatMessages.setId(snowflake.nextId());
        chatMessages.setSessionId(mqDto.getSessionId());
        chatMessages.setSenderId(mqDto.getSenderId());
        chatMessages.setMessageType(mqDto.getMessageType());
        chatMessages.setContent(mqDto.getContent());
        chatMessages.setSendAt(mqDto.getCreatedAt());
        chatMessages.setRecalled(RecalledStatusEnum.NORMAL.getCode());

        chatMessagesService.save(chatMessages);
        return chatMessages;
    }

    @Override
    public void updateChatSessionAbstract(ChatMessages chatMessages) {
        ChatSessions chatSessions = new ChatSessions();
        chatSessions.setId(chatMessages.getSessionId());
        chatSessions.setLastMessageContent(chatMessages.getContent());
        chatSessions.setLastMessageTime(chatMessages.getSendAt());

        chatSessionsService.updateById(chatSessions);
    }

    @Override
    public void increUnreadCount(Long sessionId, Long senderId) {
        chatSessionsMembersService.increUnreadCount(sessionId, senderId);
    }

    @Override
    public void joinTimeNestGroup(Long userId) {
        ChatSessionsMembers chatSessionsMembers = new ChatSessionsMembers();
        chatSessionsMembers.setSessionId(3L);
        chatSessionsMembers.setUserId(userId);
        chatSessionsMembers.setRole(ChatSessionRoleType.MEMBER.getCode());

        chatSessionsMembersService.save(chatSessionsMembers);
    }

    private ChatSessionBo createSession(Long targetId, long currentUserId) {
        // 创建新的单聊会话
        ChatSessions chatSessions = createChatSessions();
        Long sessionId = chatSessions.getId();
        createChatSessionsMembers(sessionId, currentUserId, targetId);
        // 构造结果返回
        ChatSessionBo chatSessionBo = new ChatSessionBo();
        chatSessionBo.setSessionId(sessionId);
        chatSessionBo.setSessionType(ChatSessionsTypeEnum.SINGLE.getCode());
        Users usersByCache = userService.getUsersByCache(targetId.toString());
        chatSessionBo.setDisplayName(usersByCache.getNickName());
        chatSessionBo.setDisplayAvatar(usersByCache.getAvatarUrl());
        return chatSessionBo;
    }

    private void createChatSessionsMembers(Long sessionId, long currentUserId, Long targetId) {
        ChatSessionsMembers chatSessionsMembers1 = new ChatSessionsMembers();
        chatSessionsMembers1.setSessionId(sessionId);
        chatSessionsMembers1.setUserId(currentUserId);
        chatSessionsMembers1.setRole(ChatSessionRoleType.MEMBER.getCode());

        ChatSessionsMembers chatSessionsMembers2 = new ChatSessionsMembers();
        chatSessionsMembers2.setSessionId(sessionId);
        chatSessionsMembers2.setUserId(targetId);
        chatSessionsMembers2.setRole(ChatSessionRoleType.MEMBER.getCode());

        ArrayList<ChatSessionsMembers> chatSessionsMembers = new ArrayList<>();
        chatSessionsMembers.add(chatSessionsMembers1);
        chatSessionsMembers.add(chatSessionsMembers2);
        chatSessionsMembersService.saveBatch(chatSessionsMembers);
    }

    private ChatSessions createChatSessions() {
        ChatSessions chatSessions = new ChatSessions();
        chatSessions.setSessionType(ChatSessionsTypeEnum.SINGLE.getCode());
        chatSessions.setIsDeleted(IsDeletedEnum.NOT_DELETED.getCode());
        chatSessionsService.save(chatSessions);
        return chatSessions;
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
        List<ChatSessionTargetUserBo> targetUsers = chatSessionsMembersMapper.queryTargetUsersWithUserInfo(sessionIds, currentUserId);

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
