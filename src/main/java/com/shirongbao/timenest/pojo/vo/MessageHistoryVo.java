package com.shirongbao.timenest.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-07-19
 * @description: 消息历史vo类
 */
@Data
public class MessageHistoryVo {

    // 消息记录列表
    private List<ChatMessageVo> records;

    // 下一次请求时应该使用的游标值
    private Long nextCursor;

    // 是否还有更早的历史消息
    private Boolean hasMore;

}
