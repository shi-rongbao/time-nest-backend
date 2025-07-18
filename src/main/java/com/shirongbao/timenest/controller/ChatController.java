package com.shirongbao.timenest.controller;

import com.shirongbao.timenest.common.entity.PageResult;
import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.converter.ChatConverter;
import com.shirongbao.timenest.pojo.bo.ChatSessionBo;
import com.shirongbao.timenest.pojo.dto.ChatSessionDto;
import com.shirongbao.timenest.pojo.vo.ChatSessionVo;
import com.shirongbao.timenest.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
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
        PageResult<ChatSessionVo> sessionVoPageResult = ChatConverter.INSTANCE.chatSessionBoVo(sessionBoPageResult);
        return Result.success(sessionVoPageResult);
    }

}
