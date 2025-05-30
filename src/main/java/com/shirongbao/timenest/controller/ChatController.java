package com.shirongbao.timenest.controller;

import com.shirongbao.timenest.service.chat.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: ShiRongbao
 * @date: 2025-05-29
 * @description: 聊天接口控制器
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final UserPresenceService userPresenceService;
}
