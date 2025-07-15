package com.shirongbao.timenest.config;

import com.shirongbao.timenest.interceptor.AuthHandshakeInterceptor;
import com.shirongbao.timenest.interceptor.LoginHandshakeInterceptor;
import com.shirongbao.timenest.websocket.LoginWebSocketHandler;
import com.shirongbao.timenest.websocket.MainWebSocketHandler;
import lombok.RequiredArgsConstructor;
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

    // 登录组件
    private final LoginWebSocketHandler loginWebSocketHandler;

    private final LoginHandshakeInterceptor loginHandshakeInterceptor;

    // 注入新的主通道（聊天、通知）组件
    private final MainWebSocketHandler mainWebSocketHandler;

    private final AuthHandshakeInterceptor authHandshakeInterceptor;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 1.用于扫码登录的临时连接端点
        registry.addHandler(loginWebSocketHandler, "/ws/wx/login")
                .addInterceptors(loginHandshakeInterceptor)
                .setAllowedOrigins("*");

        // 2.注册用于处理所有已登录用户实时业务的主通道端点
        registry.addHandler(mainWebSocketHandler, "/ws/user")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}