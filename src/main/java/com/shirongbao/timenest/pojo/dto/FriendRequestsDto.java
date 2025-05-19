package com.shirongbao.timenest.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友申请dto类
 */
@Data
public class FriendRequestsDto {

    @NotNull(message = "好友申请id不能为空")
    private Long friendRequestId;

    @NotNull(message = "处理结果不能为空")
    private Integer processingResult;

}
