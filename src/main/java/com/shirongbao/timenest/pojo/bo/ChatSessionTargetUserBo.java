package com.shirongbao.timenest.pojo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: ShiRongbao
 * @date: 2025-07-18
 * @description: 单聊目标用户信息BO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionTargetUserBo {

    private Long sessionId;

    private Long targetUserId;

    private String targetNickName;

    private String targetAvatarUrl;
}
