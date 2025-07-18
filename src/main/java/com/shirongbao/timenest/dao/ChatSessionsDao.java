package com.shirongbao.timenest.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.timenest.pojo.entity.ChatSessions;
import com.shirongbao.timenest.pojo.vo.ChatSessionVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author: ShiRongbao
 * @date: 2025-07-15
 * @description: 聊天会话持久层接口
 */
public interface ChatSessionsDao extends BaseMapper<ChatSessions> {
    
    /**
     * 分页查询用户的会话列表
     *
     * @param page 分页参数
     * @param userId 用户ID
     * @param sessionType 会话类型（可为null）
     * @param keyword 搜索关键词（可为null）
     * @return 分页的会话VO对象
     */
    Page<ChatSessionVo> selectSessionVOPage(
            @Param("page") Page<ChatSessions> page,
            @Param("userId") Long userId,
            @Param("sessionType") Integer sessionType,
            @Param("keyword") String keyword
    );
    
    /**
     * 批量查询会话中的对方用户信息
     *
     * @param sessionIds 会话ID列表
     * @param userId 当前用户ID
     * @return 会话ID与对方用户信息的映射
     */
    List<Map<String, Object>> selectTargetUsersBySessionIds(
            @Param("sessionIds") List<Long> sessionIds,
            @Param("userId") Long userId
    );
}
