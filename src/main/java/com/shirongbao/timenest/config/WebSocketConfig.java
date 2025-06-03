package com.shirongbao.timenest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shirongbao.timenest.service.chat.ChatMessageService;
import com.shirongbao.timenest.websocket.ChatWebSocketHandler;
import com.shirongbao.timenest.interceptor.SaTokenWebSocketInterceptor;
import com.shirongbao.timenest.service.chat.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author: ShiRongbao
 * @date: 2025-05-30
 * @description: websocket配置
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SaTokenWebSocketInterceptor saTokenWebSocketInterceptor;

    private final ObjectMapper objectMapper;

    private final UserPresenceService userPresenceService;

    private final ChatMessageService chatMessageService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 聊天 WebSocket 端点
        registry.addHandler(chatWebSocketHandler(), "/chat")
                .addInterceptors(saTokenWebSocketInterceptor)
                .setAllowedOrigins("*");
    }

    @Bean
    public ChatWebSocketHandler chatWebSocketHandler() {
        // 在这里创建 ChatWebSocketHandler 实例，并注入它所需的依赖
        return new ChatWebSocketHandler(objectMapper, userPresenceService, chatMessageService);
    }
}