package com.shirongbao.timenest.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
/**
 * @author: ShiRongbao
 * @date: 2025-07-09
 * @description: ws/user WebSocket握手认证拦截器
 */
@Component
@Slf4j
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info("【主通道握手】收到连接请求，URI: {}", request.getURI());

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            // 从URL的查询参数中获取token
            // 前端连接格式: ws://.../ws/user?token=xxxxxxx
            String token = servletRequest.getServletRequest().getParameter("token");

            if (StringUtils.isNotBlank(token)) {
                try {
                    // 使用Sa-Token的API校验Token并获取与之关联的用户ID
                    Object loginId = StpUtil.getLoginIdByToken(token);
                    if (loginId != null) {
                        // Token验证通过，将用户ID存入WebSocket的会话属性中
                        attributes.put("userId", loginId);
                        log.info("【主通道握手】Token验证通过，用户ID: {}，允许连接。", loginId);
                        // 允许握手，继续下一步
                        return true;
                    }
                } catch (Exception e) {
                    // StpUtil.getLoginIdByToken 在token无效、过期、被顶下线等情况会抛出异常
                    log.warn("【主通道握手】Token验证失败，无效的Token: {}, 异常信息: {}", token, e.getMessage());
                    // 拒绝握手
                    return false;
                }
            }
            log.warn("【主通道握手】请求中未携带Token，连接被拒绝。");
            // 拒绝握手
            return false;
        }
        // 拒绝握手
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 此处无需特殊处理
    }
}
