package com.shirongbao.timenest.controller;

import com.shirongbao.timenest.common.entity.PageResult;
import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.converter.ChatConverter;
import com.shirongbao.timenest.pojo.bo.ChatSessionBo;
import com.shirongbao.timenest.pojo.dto.ChatSessionDto;
import com.shirongbao.timenest.pojo.vo.ChatSessionVo;
import com.shirongbao.timenest.pojo.vo.MessageHistoryVo;
import com.shirongbao.timenest.service.chat.ChatService;
import com.shirongbao.timenest.validation.FindSessionValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author: ShiRongbao
 * @date: 2025-05-29
 * @description: 聊天接口控制器
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 分页获取会话列表
    @GetMapping("/getSessions")
    public Result<PageResult<ChatSessionVo>> getSessions(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sessionType", required = false) Integer sessionType,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        ChatSessionDto chatSessionDto = new ChatSessionDto();
        chatSessionDto.setSessionType(sessionType);
        chatSessionDto.setKeyword(keyword);
        PageResult<ChatSessionBo> sessionBoPageResult = chatService.getSessions(pageNum, pageSize, chatSessionDto);
        PageResult<ChatSessionVo> sessionVoPageResult = ChatConverter.INSTANCE.chatSessionBoPageToVoPage(sessionBoPageResult);
        return Result.success(sessionVoPageResult);
    }

    // 获取单聊会话
    @PostMapping("/findSingleSession")
    public Result<ChatSessionVo> findSingleSession(@RequestBody @Validated({FindSessionValidation.class}) ChatSessionDto chatSessionDto) {
        Long targetId = chatSessionDto.getUserId();
        ChatSessionBo chatSessionBo = chatService.findSingleSession(targetId);
        ChatSessionVo chatSessionVo = ChatConverter.INSTANCE.chatSessionBoToVo(chatSessionBo);
        return Result.success(chatSessionVo);
    }

    // 获取历史消息
    @GetMapping("/{sessionId}/getHistoryMessage")
    public Result<MessageHistoryVo> getHistoryMessage(@PathVariable("sessionId") Long sessionId,
                                            @RequestParam(value = "cursor", required = false) Long cursor,
                                            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        MessageHistoryVo messageHistoryVo = chatService.getHistoryMessage(sessionId, cursor, pageSize);
        return Result.success(messageHistoryVo);
    }

}
