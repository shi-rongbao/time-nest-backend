package com.shirongbao.timenest.config;

import com.shirongbao.timenest.interceptor.LoginHandshakeInterceptor;
import com.shirongbao.timenest.websocket.LoginWebSocketHandler;
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

    private final LoginWebSocketHandler loginWebSocketHandler;

    private final LoginHandshakeInterceptor loginHandshakeInterceptor;

    /**
     * 注册WebSocket处理器
     *
     * @param registry 处理器注册器
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册处理器，并指定处理该处理器的endpoint路径
        registry.addHandler(loginWebSocketHandler, "/ws/wx/login")
                // 添加握手拦截器，用于在握手阶段传递参数
                .addInterceptors(loginHandshakeInterceptor)
                // 设置允许跨域的源，"*"表示允许所有源。在生产环境中应配置为具体的域名。
                .setAllowedOrigins("*");
    }
}