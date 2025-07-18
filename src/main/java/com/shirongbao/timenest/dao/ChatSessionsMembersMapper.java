package com.shirongbao.timenest.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.timenest.pojo.bo.ChatSessionBo;
import com.shirongbao.timenest.pojo.bo.ChatSessionTargetUserBo;
import com.shirongbao.timenest.pojo.dto.ChatSessionDto;
import com.shirongbao.timenest.pojo.entity.ChatSessionsMembers;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-07-17
 * @description: 聊天会话成员持久层接口
 */
public interface ChatSessionsMembersMapper extends BaseMapper<ChatSessionsMembers> {

    /**
     * 查询会话列表
     */
    IPage<ChatSessionBo> querySessions(Page<ChatSessionBo> page,
                                       @Param("chatSessionDto") ChatSessionDto chatSessionDto);

    /**
     * 批量查询单聊目标用户信息（JOIN用户表）
     */
    List<ChatSessionTargetUserBo> queryTargetUsersWithUserInfo(@Param("sessionIds") List<Long> sessionIds,
                                                               @Param("currentUserId") Long currentUserId);

    /**
     * 根据两个用户id查询单聊
     */
    ChatSessionBo querySingleSessionWithUserId(@Param("currentUserId") long currentUserId, @Param("targetId") Long targetId);
}