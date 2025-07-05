package com.shirongbao.timenest.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
  * @author: ShiRongbao
  * @date: 2025-07-05
  * @description: WebSocket握手拦截器
  * 在WebSocket握手阶段，从HTTP请求中提取sceneId，并将其放入WebSocket的会话属性(attributes)中。
  * 这样，在后续的WebSocket处理器中，我们就能方便地获取到这个sceneId，从而知道是哪个场景的连接。
  */
@Component
public class LoginHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * 握手前执行
     *
     * @param request    当前请求，可以通过它获取请求参数、头信息等
     * @param response   当前响应
     * @param wsHandler  将要处理该连接的WebSocket处理器
     * @param attributes 从该Map中放入的属性，可以在后续的WebSocketSession中获取到
     * @return boolean 返回true则继续握手，返回false则中断连接
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 将ServerHttpRequest转换为ServletServerHttpRequest以获取更底层的请求信息
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            // 从URL的查询参数中获取sceneId
            // 例如URL为: ws://localhost:8080/ws/login?sceneId=xxxx-xxxx-xxxx
            String sceneId = servletRequest.getServletRequest().getParameter("sceneId");

            if (StringUtils.isNotBlank(sceneId)) {
                // 如果获取到了sceneId，就把它放入attributes中
                // key可以自定义，这里我们用"sceneId"
                attributes.put("sceneId", sceneId);
                // 返回true，允许握手继续
                return true;
            }
        }
        // 如果没有获取到sceneId，则中断连接
        return false;
    }

    /**
     * 握手后执行
     *
     * @param request   当前请求
     * @param response  当前响应
     * @param wsHandler 处理器
     * @param exception 握手过程中产生的异常，如果为null表示握手成功
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 握手之后基本不需要做什么处理，这里留空即可
    }
}
