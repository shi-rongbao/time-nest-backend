package com.shirongbao.timenest.pojo.dto;

import com.shirongbao.timenest.common.entity.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: ShiRongbao
 * @date: 2025-07-15
 * @description: 聊天会话dto类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatSessionDto extends PageInfo {

    // 用户id
    private Long userId;

    // 会话类型：1-单聊；2-群聊；null-全部
    private Integer sessionType;

    // 搜索关键词（可用于搜索群名称或用户昵称）
    private String keyword;

}
