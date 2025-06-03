package com.shirongbao.timenest.interceptor;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author: ShiRongbao
 * @date: 2025-05-16
 * @description: 握手拦截器
 * 用于在 WebSocket 握手阶段校验用户身份，并将用户ID绑定到 WebSocketSession 的属性中。
 */
@Component
@Slf4j
public class SaTokenWebSocketInterceptor implements HandshakeInterceptor {

    // 定义存储用户ID的属性名
    public static final String USER_ID_ATTR = "loggedInUserId";

    /**
     * 在握手之前执行，可以在这里进行身份验证和数据准备
     *
     * @param request HTTP 请求，可以从中获取 token
     * @param response HTTP 响应
     * @param wsHandler WebSocket 处理器
     * @param attributes WebSocketSession 的属性 Map，可以在这里存入数据供 WebSocketHandler 使用
     * @return 如果返回 true，则继续握手过程；如果返回 false，则中断握手
     * @throws Exception exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        log.info("WebSocket 握手开始...");

        // 1. 获取 HttpServletRequest 对象，以便从请求头、参数或Cookie中获取 token
        HttpServletRequest servletRequest = null;
        if (request instanceof ServletServerHttpRequest) {
            servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        }

        if (servletRequest == null) {
            log.warn("无法获取 HttpServletRequest，拒绝 WebSocket 连接。");
            return false;
        }

        // 2. 从请求中获取 SaToken 的 Token
        // 默认从请求头获取 SaToken 的 token name
        String token = servletRequest.getHeader(StpUtil.getTokenName());

        if (!StringUtils.hasText(token)) {
            log.warn("WebSocket 握手失败: 未找到 SaToken Token。");
            // 401 Unauthorized
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        try {
            // 3. 使用 SaToken 校验 Token 并获取用户ID
            long userId = StpUtil.getLoginIdAsLong();

            // 4. 将用户ID存储到 WebSocketSession 的属性中，供 ChatWebSocketHandler 使用
            attributes.put(USER_ID_ATTR, userId);
            log.info("WebSocket 握手成功: 用户ID {} 绑定到 Session。", userId);
            // 继续握手
            return true;
        } catch (NotLoginException e) {
            log.warn("WebSocket 握手失败: SaToken 认证失败，Token: {}", token, e);
            // 401 Unauthorized
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        } catch (Exception e) {
            log.error("WebSocket 握手过程中发生未知异常: {}", e.getMessage(), e);
            // 500 Internal Server Error
            response.setStatusCode(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    /**
     * 在握手之后执行
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param wsHandler WebSocket 处理器
     * @param exception 握手过程中发生的异常
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket 握手完成但发生异常: {}", exception.getMessage(), exception);
        } else {
            log.info("WebSocket 握手完成。");
        }
    }
}